package com.xiaoniucode.etp.client;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.common.ConfigUtils;
import com.xiaoniucode.etp.common.Constants;
import com.xiaoniucode.etp.common.LogbackConfigurator;
import com.xiaoniucode.etp.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端服务启动类
 *
 * @author liuxin
 */
public class TunnelClientStartup {
    private static final Logger logger = LoggerFactory.getLogger(TunnelClientStartup.class);
    private static TunnelClient tunnelClient;

    static {
        System.setProperty("io.netty.leakDetection.level", "SIMPLE");
    }

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, Constants.DEFAULT_CLIENT_CONFIG_NAME);
        if (!StringUtils.hasText(configPath)) {
            System.err.println("请指定配置文件路径！");
            return;
        }
        Config.init(configPath);
        initLogback();
        Config config = Config.get();
        registerShutdownHook();
        tunnelClient = new TunnelClient(config.getServerAddr(), config.getServerPort(), config.getSecretKey(), config.isTls());
        tunnelClient.setMaxDelaySec(config.getMaxDelaySec());
        tunnelClient.setInitialDelaySec(config.getInitialDelaySec());
        tunnelClient.setMaxRetries(config.getMaxRetries());
        tunnelClient.start();
    }

    private static void initLogback() {
        Config config = Config.get();
        String leve = config.getLogLevel();
        String path = config.getLogPath();
        String pattern = config.getLogPattern();
        new LogbackConfigurator.LogbackConfigBuilder()
            .setLogFilePath(StringUtils.hasText(path) ? path : (Constants.LOG_BASE_PATH + Constants.DEFAULT_CLIENT_LOG_NAME))
            .setArchiveFilePattern(StringUtils.hasText(pattern) ? pattern : (Constants.LOG_BASE_PATH + Constants.DEFAULT_CLIENT_LOG_PATTERN))
            .setLogLevel(!StringUtils.hasText(leve) ? Level.INFO : Level.toLevel(leve, Level.INFO))
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
