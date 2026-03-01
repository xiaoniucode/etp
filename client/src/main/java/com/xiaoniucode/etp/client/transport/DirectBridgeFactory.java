package com.xiaoniucode.etp.client.transport;

import io.netty.channel.Channel;

public class DirectBridgeFactory {
    /**
     * 创建双向桥接
     *
     * @param tunnel 穿透隧道
     * @param server 真实服务
     */
    public static void bridge(Channel tunnel, Channel server) {
        // Tunnel -> Server 方向
        tunnel.pipeline().addLast(new DirectChannelBridge(server, "Tunnel->Server"));

        // Server -> Tunnel 方向
        server.pipeline().addLast(new DirectChannelBridge(tunnel, "Server->Tunnel"));
    }
}
