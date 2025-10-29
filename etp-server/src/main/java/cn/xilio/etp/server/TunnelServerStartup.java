package cn.xilio.etp.server;

import ch.qos.logback.classic.Level;
import cn.xilio.etp.common.ConfigUtils;
import cn.xilio.etp.common.LogbackConfigurator;
import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.server.store.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author liuxin
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
                .setLogLevel(Level.INFO)
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
            logger.error("{}端口已经被占用",bindPort);
            return;
        }
        registerShutdownHook(tunnelServer);
        tunnelServer = new TunnelServer();
        tunnelServer.setHost(Config.getInstance().getHost());
        tunnelServer.setPort(bindPort);
        tunnelServer.setSsl(Config.getInstance().isSsl());

        tunnelServer.start();

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
