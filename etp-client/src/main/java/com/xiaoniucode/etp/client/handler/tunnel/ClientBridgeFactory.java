package com.xiaoniucode.etp.client.handler.tunnel;

import io.netty.channel.Channel;

public class ClientBridgeFactory {
    /**
     * 创建双向桥接
     *
     * @param tunnel 穿透隧道
     * @param server 真实服务
     */
    public static void bridge(Channel tunnel, Channel server) {
        // Tunnel → Server 方向
        tunnel.pipeline().addLast(new ClientChannelBridge(server, "Tunnel->Server"));

        // Server → Tunnel 方向
        server.pipeline().addLast(new ClientChannelBridge(tunnel, "Server->Tunnel"));
    }
}
