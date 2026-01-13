package com.xiaoniucode.etp.client;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.common.ConfigUtils;
import com.xiaoniucode.etp.common.Constants;
import com.xiaoniucode.etp.common.LogConfig;
import com.xiaoniucode.etp.common.LogbackConfigurator;
import com.xiaoniucode.etp.common.StringUtils;
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
        if (!StringUtils.hasText(configPath)) {
            System.err.println("请指定配置文件路径！");
            return;
        }
        Config.init(configPath);
        initLogback();
        Config config = Config.get();
        registerShutdownHook();
        tunnelClient = TunnelClient.get();
        tunnelClient.setMaxDelaySec(config.getMaxDelaySec());
        tunnelClient.setInitialDelaySec(config.getInitialDelaySec());
        tunnelClient.setMaxRetries(config.getMaxRetries());
        tunnelClient.setServerAddr(config.getServerAddr());
        tunnelClient.setServerPort(config.getServerPort());
        tunnelClient.setSecretKey(config.getSecretKey());
        tunnelClient.setTls(config.isTls());
        tunnelClient.start();
    }

    private static void initLogback() {
        LogConfig log = Config.get().getLogConfig();
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
