package cn.xilio.etp.server;

import cn.xilio.etp.common.ConfigUtils;
import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.common.ansi.AnsiLog;
import cn.xilio.etp.server.store.Config;

/**
 * @author xilio
 */
public class TunnelServerStartup {
    private static TunnelServer tunnelServer;

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, "etps.toml");
        if (configPath == null) {
            return;
        }
        //初始化代理配置信息
        Config.init(configPath);
        //检查端口是否被占用
        Integer bindPort = Config.getInstance().getBindPort();
        if (PortChecker.isPortOccupied(bindPort)) {
            AnsiLog.error("端口被占用...");
            return;
        }
        registerShutdownHook(tunnelServer);
        tunnelServer = new TunnelServer();
        tunnelServer.setPort(bindPort);
        tunnelServer.setSsl(Config.getInstance().isSsl());

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
