package cn.xilio.etp.server;

import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.server.store.ProxyManager;

/**
 * @author xilio
 */
public class TunnelServerStartup {
    public static void main(String[] args) {
        //初始化代理配置信息
        ProxyManager.init(args[1]);
        //检查端口是否被占用
        Integer bindPort = ProxyManager.getInstance().getBindPort();
        PortChecker.killPort(bindPort);
        TunnelServer tunnelServer = new TunnelServer();
        tunnelServer.setPort(bindPort);
        tunnelServer.setSsl(true);
        tunnelServer.start();
    }
}
