package cn.xilio.etp.server;

import ch.qos.logback.classic.Level;
import cn.xilio.etp.common.ConfigUtils;
import cn.xilio.etp.common.LogbackConfigurator;
import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.common.ansi.AnsiLog;
import cn.xilio.etp.server.store.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author xilio
 */
public class TunnelServerStartup {
    private static TunnelServer tunnelServer;
    private static final String DEFAULT_CONFIG_NAME = "etps.toml";
    private static final Logger logger = LoggerFactory.getLogger(TunnelServerStartup.class);

    // 初始化日志配置
    static {
        new LogbackConfigurator.LogbackConfigBuilder()
                .setLogFilePath("logs" + File.separator + "etps.log")
                .setArchiveFilePattern("logs" + File.separator + "etps.%d{yyyy-MM-dd}.log")
                .setLogLevel(Level.WARN)
                .build()
                .configureLogback();
    }

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, DEFAULT_CONFIG_NAME);
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
        logger.info("代理服务启动成功，监听端口：{}", tunnelServer.getPort());
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
