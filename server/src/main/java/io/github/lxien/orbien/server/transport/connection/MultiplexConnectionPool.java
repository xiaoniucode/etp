package io.github.lxien.orbien.server.transport.connection;

import io.github.lxien.orbien.core.transport.TunnelEntry;
import io.github.lxien.orbien.core.transport.api.TransportPoolKey;
import io.github.lxien.orbien.core.enums.TransportProtocol;
import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class MultiplexConnectionPool {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MultiplexConnectionPool.class);

    private final ConcurrentHashMap<String, AgentPool> agentPools = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WaitKey, List<AcquireWaiter>> waiters = new ConcurrentHashMap<>();

    public void acquireAsync(String agentId,
                             TransportProtocol protocol,
                             boolean isTls,
                             long timeoutMs,
                             EventLoop eventLoop,
                             Promise<TunnelEntry> promise) {
        TunnelEntry entry = tryAcquire(agentId, protocol, isTls);
        if (entry != null) {
            promise.setSuccess(entry);
            return;
        }

        WaitKey key = new WaitKey(agentId, protocol, isTls);
        AcquireWaiter waiter = new AcquireWaiter(promise);
        List<AcquireWaiter> list = waiters.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (list) {
            list.add(waiter);
        }

        entry = tryAcquire(agentId, protocol, isTls);
        if (entry != null) {
            removeWaiter(key, waiter);
            if (!promise.isDone()) {
                promise.setSuccess(entry);
            }
            return;
        }

        ScheduledFuture<?> timeoutFuture = eventLoop.schedule(() -> {
            removeWaiter(key, waiter);
            if (!promise.isDone()) {
                logger.warn("[传输] 等待多路复用隧道入池超时 agentId={} protocol={} encrypt={} timeoutMs={}",
                        agentId, protocol.getName(), isTls, timeoutMs);
                promise.setFailure(new IllegalStateException("等待多路复用隧道超时"));
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        waiter.timeoutFuture = timeoutFuture;
        promise.addListener(f -> {
            if (waiter.timeoutFuture != null) {
                waiter.timeoutFuture.cancel(false);
            }
        });
    }

    private TunnelEntry tryAcquire(String agentId, TransportProtocol protocol, boolean isTls) {
        AgentPool pool = agentPools.get(agentId);
        if (pool == null) {
            return null;
        }
        TunnelEntry entry = pool.acquire(protocol, isTls);
        if (entry == null || !entry.isActive()) {
            return null;
        }
        return entry;
    }

    public void setChannel(String agentId, TransportProtocol protocol, boolean isTls, TunnelEntry entry) {
        if (agentId == null || entry == null) {
            throw new IllegalArgumentException("客户端 ID或隧道节点不能为空");
        }
        logger.debug("[传输] 客户端 {} 注册多路复用隧道 protocol={} encrypt={} tunnelId={} channelClass={}",
                agentId, protocol.getName(), isTls, entry.getTunnelId(),
                entry.getChannel().getClass().getSimpleName());
        AgentPool pool = agentPools.computeIfAbsent(agentId, k -> new AgentPool());
        pool.setChannel(protocol, isTls, entry);
        notifyWaiters(agentId, protocol, isTls, entry);
    }

    private void notifyWaiters(String agentId, TransportProtocol protocol, boolean isTls, TunnelEntry entry) {
        WaitKey key = new WaitKey(agentId, protocol, isTls);
        List<AcquireWaiter> list = waiters.remove(key);
        if (list == null || list.isEmpty()) {
            return;
        }
        List<AcquireWaiter> snapshot;
        synchronized (list) {
            snapshot = new ArrayList<>(list);
            list.clear();
        }
        for (AcquireWaiter waiter : snapshot) {
            if (waiter.timeoutFuture != null) {
                waiter.timeoutFuture.cancel(false);
            }
            if (!waiter.promise.isDone()) {
                waiter.promise.setSuccess(entry);
            }
        }
    }

    private void removeWaiter(WaitKey key, AcquireWaiter waiter) {
        List<AcquireWaiter> list = waiters.get(key);
        if (list == null) {
            return;
        }
        synchronized (list) {
            list.remove(waiter);
            if (list.isEmpty()) {
                waiters.remove(key, list);
            }
        }
    }

    public void offline(String agentId) {
        if (agentId == null) {
            return;
        }
        AgentPool pool = agentPools.remove(agentId);
        if (pool != null) {
            pool.offline();
        }
        failWaitersForAgent(agentId);
    }

    private void failWaitersForAgent(String agentId) {
        List<WaitKey> keys = waiters.keySet().stream()
                .filter(k -> Objects.equals(k.agentId, agentId))
                .toList();
        for (WaitKey key : keys) {
            List<AcquireWaiter> list = waiters.remove(key);
            if (list == null) {
                continue;
            }
            for (AcquireWaiter waiter : list) {
                if (waiter.timeoutFuture != null) {
                    waiter.timeoutFuture.cancel(false);
                }
                if (!waiter.promise.isDone()) {
                    waiter.promise.setFailure(new IllegalStateException("客户端已离线"));
                }
            }
        }
    }

    public void removeByChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        agentPools.forEach((agentId, pool) -> {
            if (pool.removeByChannel(channel)) {
                logger.warn("[传输] 客户端 {} 数据隧道已断开并从连接池移除 channelClass={}",
                        agentId, channel.getClass().getSimpleName());
            }
        });
    }

    static class AgentPool {
        private final Map<TransportPoolKey, TunnelEntry> entries = new ConcurrentHashMap<>();

        public TunnelEntry acquire(TransportProtocol protocol, boolean isTls) {
            return entries.get(TransportPoolKey.multiplex(protocol, isTls));
        }

        public void setChannel(TransportProtocol protocol, boolean isTls, TunnelEntry entry) {
            entries.put(TransportPoolKey.multiplex(protocol, isTls), entry);
        }

        public void offline() {
            entries.values().forEach(entry -> ChannelUtils.closeOnFlush(entry.getChannel()));
            entries.clear();
        }

        boolean removeByChannel(Channel channel) {
            return entries.entrySet().removeIf(e -> {
                TunnelEntry entry = e.getValue();
                return entry != null && entry.getChannel() == channel;
            });
        }
    }

    private record WaitKey(String agentId, TransportProtocol protocol, boolean tls) {
    }

    private static final class AcquireWaiter {
        private final Promise<TunnelEntry> promise;
        private volatile ScheduledFuture<?> timeoutFuture;

        private AcquireWaiter(Promise<TunnelEntry> promise) {
            this.promise = promise;
        }
    }
}
