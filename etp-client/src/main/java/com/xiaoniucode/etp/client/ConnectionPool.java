package com.xiaoniucode.etp.client;

import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionPool {
    private static final int MAX_DATA_TUNNEL_CHANNEL_POOL_SIZE = 1000;
    private static final Queue<Channel> tunnelChannelPool = new ConcurrentLinkedQueue<>();


    public static void removeDataTunnelChanel(Channel dataTunnelChannel) {
        tunnelChannelPool.remove(dataTunnelChannel);
    }
    public static CompletableFuture<Channel> borrowDataTunnelChannel(){
        CompletableFuture<Channel> future = new CompletableFuture<>();
        Channel dataTunnelChannel = tunnelChannelPool.poll();
        if (dataTunnelChannel != null) {
            future.complete(dataTunnelChannel);
            return future;
        }

        Bootstrap tunnelBootstrap = ChannelManager.getControlBootstrap();
        Channel control = ChannelManager.getControlChannel();
        String serverAddr = control.attr(EtpConstants.SERVER_DDR).get();
        Integer serverPort = control.attr(EtpConstants.SERVER_PORT).get();

        tunnelBootstrap.connect(serverAddr, serverPort).addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                future.complete(f.channel());
            } else {
                future.completeExceptionally(f.cause());
            }
        });

        return future;
    }

    public static void returnDataTunnelChanel(Channel tunnel) {
        if (tunnelChannelPool.size() > MAX_DATA_TUNNEL_CHANNEL_POOL_SIZE) {
            tunnel.close();
        } else {
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
            tunnel.attr(EtpConstants.REAL_SERVER_CHANNEL).getAndSet(null);
            tunnelChannelPool.offer(tunnel);
        }
    }
}
