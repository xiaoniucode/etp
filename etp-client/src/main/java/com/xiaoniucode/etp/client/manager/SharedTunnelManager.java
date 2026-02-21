package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.ConfigUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 共享隧道管理器
 * 所有共享隧道代理共用一个连接
 */
public class SharedTunnelManager {
    private static final AtomicReference<Channel> sharedTunnel = new AtomicReference<>();
    private static volatile boolean connecting = false;

    /**
     * 获取共享隧道
     * 如果没有则创建，有则直接返回
     */
    public static CompletableFuture<Channel> acquire() {
        CompletableFuture<Channel> future = new CompletableFuture<>();

        Channel channel = sharedTunnel.get();
        if (channel != null && channel.isActive()) {
            future.complete(channel);
            return future;
        }

        synchronized (SharedTunnelManager.class) {
            channel = sharedTunnel.get();
            if (channel != null && channel.isActive()) {
                future.complete(channel);
                return future;
            }

            if (connecting) {
                future.completeExceptionally(new IllegalStateException("共享隧道正在连接中"));
                return future;
            }

            connecting = true;
        }

        AppConfig config = ConfigUtils.getConfig();
        Bootstrap tunnelBootstrap = BootstrapManager.getTunnelBootstrap();

        tunnelBootstrap.connect(config.getServerAddr(), config.getServerPort())
                .addListener((ChannelFutureListener) f -> {
                    synchronized (SharedTunnelManager.class) {
                        connecting = false;

                        if (f.isSuccess()) {
                            Channel newChannel = f.channel();
                            newChannel.closeFuture().addListener(future1 -> {
                                sharedTunnel.compareAndSet(newChannel, null);
                            });

                            sharedTunnel.set(newChannel);
                            future.complete(newChannel);
                        } else {
                            sharedTunnel.set(null);
                            future.completeExceptionally(f.cause());
                        }
                    }
                });

        return future;
    }

    /**
     * 释放共享隧道，不是真正的释放，只是标记为可用
     */
    public static void release(Channel tunnel) {
        Channel current = sharedTunnel.get();
        if (current == tunnel && tunnel.isActive()) {
            //确保每次使用后都恢复可读状态
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
        } else {
            // 不是共享隧道，直接关闭
            tunnel.close();
        }
    }

    /**
     * 关闭共享隧道
     */
    public static void close() {
        Channel channel = sharedTunnel.getAndSet(null);
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    /**
     * 检查共享隧道是否可用
     */
    public static boolean isAvailable() {
        Channel channel = sharedTunnel.get();
        return channel != null && channel.isActive();
    }

    /**
     * 安全的业务处理模板
     */
    public static void executeWithTunnel(Consumer<Channel> callback) throws InterruptedException, ExecutionException {
        Channel tunnel = acquire().get();
        try {
            callback.accept(tunnel);
        } finally {
            release(tunnel);
        }
    }
}