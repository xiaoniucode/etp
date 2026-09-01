package io.github.lxien.orbien.client.transport.connection;

import io.github.lxien.orbien.client.util.UUIDGenerator;
import io.github.lxien.orbien.core.enums.TunnelType;
import io.github.lxien.orbien.core.transport.TunnelEntry;
import io.github.lxien.orbien.core.transport.api.TransportPoolKey;
import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ProtocolDirectPool {

    private static final int MAX_TUNNEL_POOL_SIZE = 100;
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ProtocolDirectPool.class);
    private final TransportPoolKey poolKey;
    private final Map<String, TunnelEntry> plainTunnels = new ConcurrentHashMap<>(5);
    private final Map<String, TunnelEntry> encryptTunnels = new ConcurrentHashMap<>(5);
    private final List<CompletableFuture<TunnelEntry>> plainWaiters = new ArrayList<>();
    private final List<CompletableFuture<TunnelEntry>> encryptWaiters = new ArrayList<>();

    public ProtocolDirectPool(TransportPoolKey poolKey) {
        this.poolKey = poolKey;
    }

    public TunnelEntry borrow(boolean isEncrypt) {
        Map<String, TunnelEntry> tunnels = isEncrypt ? encryptTunnels : plainTunnels;
        for (Map.Entry<String, TunnelEntry> mapEntry : tunnels.entrySet()) {
            TunnelEntry entry = mapEntry.getValue();
            if (entry.isActive()) {
                return tunnels.remove(mapEntry.getKey());
            }
            if (!entry.isChannelAlive()) {
                removeTunnel(entry.getTunnelId());
            }
        }
        logger.debug("池中没有可用的活跃{}隧道: {}", isEncrypt ? "加密" : "明文", poolKey);
        return null;
    }

    public boolean hasAliveTunnel(boolean isEncrypt) {
        Map<String, TunnelEntry> tunnels = isEncrypt ? encryptTunnels : plainTunnels;
        for (TunnelEntry entry : tunnels.values()) {
            if (entry.isChannelAlive()) {
                return true;
            }
        }
        return false;
    }

    public TunnelEntry createTunnel(Channel channel, boolean isEncrypt) {
        if (plainTunnels.size() + encryptTunnels.size() >= MAX_TUNNEL_POOL_SIZE) {
            return null;
        }
        String tunnelId = UUIDGenerator.generate();
        TunnelEntry tunnelEntry = new TunnelEntry(
                tunnelId, poolKey.protocol(), isEncrypt, channel, TunnelType.DIRECT);
        tunnelEntry.setActive(false);
        if (isEncrypt) {
            encryptTunnels.putIfAbsent(tunnelId, tunnelEntry);
        } else {
            plainTunnels.putIfAbsent(tunnelId, tunnelEntry);
        }
        return tunnelEntry;
    }

    public TunnelEntry activateTunnel(String tunnelId) {
        TunnelEntry entry = plainTunnels.get(tunnelId);
        if (entry == null) {
            entry = encryptTunnels.get(tunnelId);
        }
        if (entry != null) {
            entry.setActive(true);
            completeWaiters(entry.isEncrypt(), entry);
        }
        return entry;
    }

    public CompletableFuture<TunnelEntry> awaitReady(boolean isEncrypt, long timeoutMs, EventLoop eventLoop) {
        TunnelEntry ready = peekReady(isEncrypt);
        if (ready != null) {
            return CompletableFuture.completedFuture(borrow(isEncrypt));
        }
        CompletableFuture<TunnelEntry> future = new CompletableFuture<>();
        List<CompletableFuture<TunnelEntry>> waiters = isEncrypt ? encryptWaiters : plainWaiters;
        synchronized (waiters) {
            waiters.add(future);
            TunnelEntry borrowed = borrow(isEncrypt);
            if (borrowed != null) {
                waiters.remove(future);
                future.complete(borrowed);
                return future;
            }
        }

        ScheduledFuture<?> timeoutFuture = eventLoop.schedule(() -> {
            synchronized (waiters) {
                waiters.remove(future);
            }
            future.completeExceptionally(new IllegalStateException(
                    "等待独立隧道就绪超时 protocol=" + poolKey + " encrypt=" + isEncrypt));
        }, timeoutMs, TimeUnit.MILLISECONDS);

        future.whenComplete((r, e) -> timeoutFuture.cancel(false));
        return future;
    }

    private TunnelEntry peekReady(boolean isEncrypt) {
        Map<String, TunnelEntry> tunnels = isEncrypt ? encryptTunnels : plainTunnels;
        for (TunnelEntry entry : tunnels.values()) {
            if (entry.isActive()) {
                return entry;
            }
        }
        return null;
    }

    private void completeWaiters(boolean isEncrypt, TunnelEntry entry) {
        List<CompletableFuture<TunnelEntry>> waiters = isEncrypt ? encryptWaiters : plainWaiters;
        List<CompletableFuture<TunnelEntry>> snapshot;
        synchronized (waiters) {
            if (waiters.isEmpty()) {
                return;
            }
            snapshot = new ArrayList<>(waiters);
            waiters.clear();
        }
        CompletableFuture<TunnelEntry> first = snapshot.remove(0);
        TunnelEntry borrowed = borrow(isEncrypt);
        if (borrowed != null) {
            first.complete(borrowed);
        } else {
            Map<String, TunnelEntry> tunnels = isEncrypt ? encryptTunnels : plainTunnels;
            tunnels.remove(entry.getTunnelId());
            first.complete(entry);
        }
        if (!snapshot.isEmpty()) {
            synchronized (waiters) {
                waiters.addAll(snapshot);
            }
        }
    }

    public TunnelEntry findByTunnelId(String tunnelId) {
        if (tunnelId == null) {
            return null;
        }
        TunnelEntry entry = plainTunnels.get(tunnelId);
        return entry != null ? entry : encryptTunnels.get(tunnelId);
    }

    public void release(TunnelEntry entry) {
        if (entry == null) {
            return;
        }
        Channel tunnel = entry.getChannel();
        if (plainTunnels.size() + encryptTunnels.size() > MAX_TUNNEL_POOL_SIZE) {
            tunnel.close();
            removeTunnel(entry.getTunnelId());
        } else {
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
            entry.setActive(true);
            if (entry.isEncrypt()) {
                encryptTunnels.putIfAbsent(entry.getTunnelId(), entry);
            } else {
                plainTunnels.putIfAbsent(entry.getTunnelId(), entry);
            }
            completeWaiters(entry.isEncrypt(), entry);
        }
    }

    public void removeTunnel(String tunnelId) {
        TunnelEntry entry = plainTunnels.remove(tunnelId);
        if (entry == null) {
            entry = encryptTunnels.remove(tunnelId);
        }
        if (entry != null && entry.getChannel() != null && entry.getChannel().isActive()) {
            entry.getChannel().close();
        }
    }

    public boolean removeByChannel(Channel channel) {
        if (channel == null) {
            return false;
        }
        return removeByChannel(plainTunnels, channel) || removeByChannel(encryptTunnels, channel);
    }

    private boolean removeByChannel(Map<String, TunnelEntry> tunnels, Channel channel) {
        for (Map.Entry<String, TunnelEntry> entry : tunnels.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getChannel() == channel) {
                tunnels.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public void closeAll() {
        plainTunnels.values().forEach(entry -> ChannelUtils.closeOnFlush(entry.getChannel()));
        encryptTunnels.values().forEach(entry -> ChannelUtils.closeOnFlush(entry.getChannel()));
        plainTunnels.clear();
        encryptTunnels.clear();
        failWaiters(plainWaiters);
        failWaiters(encryptWaiters);
    }

    private void failWaiters(List<CompletableFuture<TunnelEntry>> waiters) {
        List<CompletableFuture<TunnelEntry>> snapshot;
        synchronized (waiters) {
            snapshot = new ArrayList<>(waiters);
            waiters.clear();
        }
        IllegalStateException cause = new IllegalStateException("独立连接池已关闭");
        for (CompletableFuture<TunnelEntry> waiter : snapshot) {
            waiter.completeExceptionally(cause);
        }
    }
}
