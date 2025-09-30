package cn.xilio.etp.server;

import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.common.ansi.AnsiLog;
import cn.xilio.etp.server.store.ProxyManager;

/**
 * @author xilio
 */
public class TunnelServerStartup {
    private static TunnelServer tunnelServer;

    public static void main(String[] args) {
        //初始化代理配置信息
        ProxyManager.init(args[1]);
        //检查端口是否被占用
        Integer bindPort = ProxyManager.getInstance().getBindPort();
        if (PortChecker.isPortOccupied(bindPort)) {
            AnsiLog.error("端口被占用...");
            return;
        }
        registerShutdownHook(tunnelServer);
        tunnelServer = new TunnelServer();
        tunnelServer.setPort(bindPort);

        tunnelServer.start();
        AnsiLog.info("代理服务启动成功...");
    }

    private static void registerShutdownHook(TunnelServer tunnelServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (tunnelServer != null) {
                tunnelServer.stop();
                PortChecker.killPort(tunnelServer.getPort());
            }
        }));
    }
}
