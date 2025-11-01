package cn.xilio.etp.server;

import cn.xilio.etp.server.store.Config;
import cn.xilio.etp.server.web.framework.NettyWebServer;

/**
 * @author liuxin
 */
public final class ServerFactory {
    public static TunnelServer createTunnelServer() {
        TunnelServer tunnelServer = new TunnelServer();
        tunnelServer.setHost(Config.getInstance().getHost());
        tunnelServer.setPort(Config.getInstance().getBindPort());
        tunnelServer.setSsl(Config.getInstance().isSsl());
        return tunnelServer;
    }

    public static NettyWebServer createWebServer() {
        Config config = Config.getInstance();
        NettyWebServer web = new NettyWebServer();
        if (config.getDashboard().getPort() != null) {
            web.setPort(config.getDashboard().getPort());
        }
        return web;
    }
}
