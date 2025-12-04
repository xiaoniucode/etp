package cn.xilio.etp.server;

import cn.xilio.etp.server.config.AppConfig;
import cn.xilio.etp.server.web.server.NettyWebServer;

/**
 * @author liuxin
 */
public final class ServerFactory {
    public static TunnelServer createTunnelServer() {
        TunnelServer tunnelServer = new TunnelServer();
        tunnelServer.setHost(AppConfig.get().getHost());
        tunnelServer.setPort(AppConfig.get().getBindPort());
        tunnelServer.setTls(AppConfig.get().isTls());
        return tunnelServer;
    }

    public static NettyWebServer createWebServer() {
        AppConfig config = AppConfig.get();
        NettyWebServer web = new NettyWebServer();
        web.setAddr(config.getDashboard().getAddr());
        web.setPort(config.getDashboard().getPort());
        return web;
    }
}
