package com.xiaoniucode.etp.client;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.DefaultAppConfig;
import com.xiaoniucode.etp.client.config.TomlConfigSource;
import com.xiaoniucode.etp.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理客户端启动入口
 *
 * @author liuxin
 */
public class TunnelClientStartup {
    private static final Logger logger = LoggerFactory.getLogger(TunnelClientStartup.class);
    private static TunnelClient tunnelClient;

    static {
        System.setProperty("io.netty.leakDetection.level", "DISABLED");
    }

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, Constants.CLIENT_CONFIG_NAME);
        AppConfig config;
        if (StringUtils.hasText(configPath)) {
            TomlConfigSource configSource = new TomlConfigSource(configPath);
            config = configSource.load();
        } else {
            config = new DefaultAppConfig.Builder().build();
        }
        initLogback(config);
        registerShutdownHook();
        tunnelClient = new TunnelClient(config);
        tunnelClient.start();
    }

    private static void initLogback(AppConfig config) {
        LogConfig log = config.getLogConfig();
        new LogbackConfigurator.Builder()
                .setPath(log.getPath())
                .setLogPattern(log.getLogPattern())
                .setArchivePattern(log.getArchivePattern())
                .setLogLevel(log.getLevel())
                .setLogName(log.getName())
                .setMaxHistory(log.getMaxHistory())
                .setTotalSizeCap(log.getTotalSizeCap())
                .addLogger("io.netty.channel.ChannelHandlerMask", Level.INFO)
                .build()
                .configure();
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
