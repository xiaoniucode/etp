package io.github.lxien.orbien.client.transport.connection;

import io.github.lxien.orbien.client.util.UUIDGenerator;
import io.github.lxien.orbien.core.enums.TunnelType;
import io.github.lxien.orbien.core.transport.TunnelEntry;
import io.github.lxien.orbien.core.transport.api.TransportPoolKey;
import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProtocolMultiplexPool {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(ProtocolMultiplexPool.class);
    private final TransportPoolKey poolKey;
    private TunnelEntry tlsTunnelEntry;
    private TunnelEntry plainTunnelEntry;
    private final List<CompletableFuture<TunnelEntry>> tlsWaiters = new ArrayList<>();
    private final List<CompletableFuture<TunnelEntry>> plainWaiters = new ArrayList<>();

    public ProtocolMultiplexPool(TransportPoolKey poolKey) {
        this.poolKey = poolKey;
    }

    public synchronized TunnelEntry acquire(boolean isTls) {
        TunnelEntry tunnelEntry = isTls ? tlsTunnelEntry : plainTunnelEntry;
        if (tunnelEntry == null) {
            return null;
        }
        if (tunnelEntry.isActive()) {
            return tunnelEntry;
        }
        if (!tunnelEntry.isChannelAlive()) {
            logger.warn("多路复用连接已失效: {}", poolKey);
            clearTunnelLocked(isTls);
        }
        return null;
    }

    public synchronized boolean hasAliveTunnel(boolean isTls) {
        TunnelEntry tunnelEntry = isTls ? tlsTunnelEntry : plainTunnelEntry;
        return tunnelEntry != null && tunnelEntry.isChannelAlive();
    }

    public synchronized TunnelEntry createChannel(boolean isTls, Channel tunnel) {
        if (!tunnel.isActive()) {
            return null;
        }
        TunnelEntry existing = isTls ? tlsTunnelEntry : plainTunnelEntry;
        if (existing != null && existing.isChannelAlive()) {
            logger.debug("多路复用隧道已存在，跳过重复创建: {} encrypt={}", poolKey, isTls);
            ChannelUtils.closeOnFlush(tunnel);
            return existing;
        }
        String tunnelId = UUIDGenerator.generate();
        TunnelEntry tunnelEntry = new TunnelEntry(
                tunnelId, poolKey.protocol(), isTls, tunnel, TunnelType.MULTIPLEX);
        tunnelEntry.setActive(false);
        if (isTls) {
            this.tlsTunnelEntry = tunnelEntry;
        } else {
            this.plainTunnelEntry = tunnelEntry;
        }
        return tunnelEntry;
    }

    public synchronized TunnelEntry activeTunnel(boolean isTls) {
        TunnelEntry entry = isTls ? tlsTunnelEntry : plainTunnelEntry;
        if (entry != null) {
            entry.setActive(true);
            completeWaitersLocked(isTls, entry);
        }
        return entry;
    }

    public synchronized CompletableFuture<TunnelEntry> awaitReady(boolean isTls, long timeoutMs, EventLoop eventLoop) {
        TunnelEntry ready = acquire(isTls);
        if (ready != null) {
            return CompletableFuture.completedFuture(ready);
        }
        CompletableFuture<TunnelEntry> future = new CompletableFuture<>();
        List<CompletableFuture<TunnelEntry>> waiters = isTls ? tlsWaiters : plainWaiters;
        waiters.add(future);

        ready = acquire(isTls);
        if (ready != null) {
            waiters.remove(future);
            future.complete(ready);
            return future;
        }

        ScheduledFuture<?> timeoutFuture = eventLoop.schedule(() -> {
            synchronized (ProtocolMultiplexPool.this) {
                waiters.remove(future);
            }
            future.completeExceptionally(new IllegalStateException(
                    "等待多路复用隧道就绪超时 protocol=" + poolKey + " encrypt=" + isTls));
        }, timeoutMs, TimeUnit.MILLISECONDS);

        future.whenComplete((r, e) -> timeoutFuture.cancel(false));
        return future;
    }

    public TunnelEntry findByTunnelId(String tunnelId) {
        if (tunnelId == null) {
            return null;
        }
        synchronized (this) {
            if (tlsTunnelEntry != null && tunnelId.equals(tlsTunnelEntry.getTunnelId())) {
                return tlsTunnelEntry;
            }
            if (plainTunnelEntry != null && tunnelId.equals(plainTunnelEntry.getTunnelId())) {
                return plainTunnelEntry;
            }
        }
        return null;
    }

    public synchronized void clearTunnel(boolean isTls) {
        clearTunnelLocked(isTls);
    }

    private void clearTunnelLocked(boolean isTls) {
        if (isTls && tlsTunnelEntry != null) {
            ChannelUtils.closeOnFlush(tlsTunnelEntry.getChannel());
            this.tlsTunnelEntry = null;
            failWaitersLocked(true, new IllegalStateException("多路复用加密隧道已清理"));
            return;
        }
        if (plainTunnelEntry != null) {
            ChannelUtils.closeOnFlush(plainTunnelEntry.getChannel());
            this.plainTunnelEntry = null;
            failWaitersLocked(false, new IllegalStateException("多路复用明文隧道已清理"));
        }
    }

    public synchronized void closeAll() {
        if (tlsTunnelEntry != null) {
            ChannelUtils.closeOnFlush(tlsTunnelEntry.getChannel());
            tlsTunnelEntry = null;
        }
        if (plainTunnelEntry != null) {
            ChannelUtils.closeOnFlush(plainTunnelEntry.getChannel());
            plainTunnelEntry = null;
        }
        failWaitersLocked(true, new IllegalStateException("多路复用连接池已关闭"));
        failWaitersLocked(false, new IllegalStateException("多路复用连接池已关闭"));
    }

    private void completeWaitersLocked(boolean isTls, TunnelEntry entry) {
        List<CompletableFuture<TunnelEntry>> waiters = isTls ? tlsWaiters : plainWaiters;
        List<CompletableFuture<TunnelEntry>> snapshot = new ArrayList<>(waiters);
        waiters.clear();
        for (CompletableFuture<TunnelEntry> waiter : snapshot) {
            waiter.complete(entry);
        }
    }

    private void failWaitersLocked(boolean isTls, Throwable cause) {
        List<CompletableFuture<TunnelEntry>> waiters = isTls ? tlsWaiters : plainWaiters;
        List<CompletableFuture<TunnelEntry>> snapshot = new ArrayList<>(waiters);
        waiters.clear();
        for (CompletableFuture<TunnelEntry> waiter : snapshot) {
            waiter.completeExceptionally(cause);
        }
    }
}
