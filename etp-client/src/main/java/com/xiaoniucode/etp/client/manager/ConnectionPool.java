package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.core.constant.ChannelConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionPool {
    private static final int MAX_TUNNEL_POOL_SIZE = 1000;
    private static final Queue<Channel> pool = new ConcurrentLinkedQueue<>();

    public static CompletableFuture<Channel> acquire() {
        CompletableFuture<Channel> future = new CompletableFuture<>();
        Channel connection = pool.poll();
        if (connection != null) {
            future.complete(connection);
            return future;
        }
        AgentSessionManager.getControl().ifPresent(control -> {
            String serverAddr = control.attr(ChannelConstants.SERVER_DDR).get();
            Integer serverPort = control.attr(ChannelConstants.SERVER_PORT).get();
            Bootstrap tunnelBootstrap = BootstrapManager.getTunnelBootstrap();
            tunnelBootstrap.connect(serverAddr, serverPort).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    future.complete(f.channel());
                } else {
                    future.completeExceptionally(f.cause());
                }
            });
        });
        return future;
    }

    public static void release(Channel tunnel) {
        if (pool.size() > MAX_TUNNEL_POOL_SIZE) {
            tunnel.close();
        } else {
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
            pool.offer(tunnel);
        }
    }
}
