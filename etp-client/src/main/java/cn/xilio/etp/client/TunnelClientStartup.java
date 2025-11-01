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

    static {
        new LogbackConfigurator.LogbackConfigBuilder()
                .setLogDir("logs")
                .setLogFilePath("logs" + File.separator + "etpc.log")
                .setArchiveFilePattern("logs" + File.separator + "etpc.%d{yyyy-MM-dd}.log")
                .setLogLevel(Level.INFO)
                .build()
                .configureLogback();
    }

    private static TunnelClient tunnelClient;

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, DEFAULT_CONFIG_NAME);
        if (configPath == null) {
            return;
        }
        Config.init(configPath);
        Config config = Config.getInstance();

        registerShutdownHook();
        tunnelClient = new TunnelClient();
        tunnelClient.setServerAddr(config.getServerAddr());
        tunnelClient.setServerPort(config.getServerPort());
        tunnelClient.setSecretKey(config.getSecretKey());
        tunnelClient.setSsl(config.isSsl());
        tunnelClient.start();
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
