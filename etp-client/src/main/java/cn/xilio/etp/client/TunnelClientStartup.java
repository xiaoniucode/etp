package cn.xilio.etp.client;

import ch.qos.logback.classic.Level;
import cn.xilio.etp.common.ConfigUtils;
import cn.xilio.etp.common.LogbackConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author liuxin
 */
public class TunnelClientStartup {
    private static final Logger logger = LoggerFactory.getLogger(TunnelClientStartup.class);
    private static final String DEFAULT_CONFIG_NAME = "etpc.toml";
    private static TunnelClient tunnelClient;

    static {
        System.setProperty("io.netty.leakDetection.level", "SIMPLE");
    }

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, DEFAULT_CONFIG_NAME);
        if (configPath == null) {
            System.out.println("请指定系统配置文件");
            return;
        }
        Config.init(configPath);
        initLogback();
        Config config = Config.getInstance();
        registerShutdownHook();
        tunnelClient = new TunnelClient(config.getServerAddr(), config.getServerPort(), config.getSecretKey(), config.isTls());
        tunnelClient.start();
    }

    private static void initLogback() {
        new LogbackConfigurator.LogbackConfigBuilder()
                .setLogDir("logs")
                .setLogFilePath("logs" + File.separator + "etpc.log")
                .setArchiveFilePattern("logs" + File.separator + "etpc.%d{yyyy-MM-dd}.log")
                .setLogLevel(Level.DEBUG)
                .build()
                .configureLogback();
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (tunnelClient != null) {
                try {
                    tunnelClient.stop();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }));
    }
}
