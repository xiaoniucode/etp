package com.xiaoniucode.etp.client;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.DefaultAppConfig;
import com.xiaoniucode.etp.client.config.TomlConfigSource;
import com.xiaoniucode.etp.common.*;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.common.log.LogbackConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TunnelClientStartup {
    private static final Logger logger = LoggerFactory.getLogger(TunnelClientStartup.class);
    private static TunnelClient tunnelClient;

    static {
        System.setProperty("io.netty.leakDetection.level", "DISABLED");
    }

    public static void main(String[] args) {
        try {
            AppConfig config = buildConfig(args);
            initLogback(config);
            registerShutdownHook();
            tunnelClient = new TunnelClient(config);
            tunnelClient.start();
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            System.err.println("错误: " + e.getMessage());
            printHelp();
            System.exit(1);
        } catch (Exception e) {
            logger.error("启动失败", e);
            System.err.println("启动失败: " + e.getMessage());
            System.exit(1);
        }
    }

    private static AppConfig buildConfig(String[] args) {
        CommandLineArgs cmdArgs = new CommandLineArgs();

        cmdArgs.registerOption("config", "c", "配置文件路径", false, true);
        cmdArgs.registerOption("host", "h", "服务器地址", false, true);
        cmdArgs.registerOption("port", "p", "隧道端口", false, true);
        cmdArgs.registerOption("secretKey", "sk", "认证密钥", false, true);
        cmdArgs.registerOption("help", "help", "显示帮助信息", false, false);

        try {
            cmdArgs.parse(args);

            if (cmdArgs.has("help")) {
                printHelp();
                System.exit(0);
            }

            if (cmdArgs.has("config")) {
                return loadConfigFromFile(cmdArgs.get("config"));
            }
            return buildConfigFromCommandLine(cmdArgs);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("配置加载失败: " + e.getMessage(), e);
        }
    }

    private static AppConfig loadConfigFromFile(String configPath) {
        if (!Files.exists(Paths.get(configPath))) {
            throw new IllegalArgumentException("配置文件不存在: " + configPath);
        }
        TomlConfigSource configSource = new TomlConfigSource(configPath);
        return configSource.load();
    }

    private static AppConfig buildConfigFromCommandLine(CommandLineArgs cmdArgs) {
        DefaultAppConfig.Builder builder = DefaultAppConfig.builder();

        if (cmdArgs.has("host")) {
            builder.serverAddr(cmdArgs.get("host"));
        }
        if (cmdArgs.has("port")) {
            int port = cmdArgs.getInt("port", 9527);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("端口号必须在1-65535范围内: " + port);
            }
            builder.serverPort(port);
        }
        if (cmdArgs.has("secretKey")) {
            builder.secretKey(cmdArgs.get("secretKey"));
        }
        return builder.build();
    }

    private static void initLogback(AppConfig config) {
        LogConfig log = config.getLogConfig();
        if (log == null) {
            return;
        }
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
                    logger.error("停止客户端时发生错误", e);
                }
            }
        }));
    }

    private static void printHelp() {
        System.out.println("用法: etpc [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -c, --config <path>        配置文件路径");
        System.out.println("  -h, --host <address>       服务器地址");
        System.out.println("  -p, --port <port>          服务器端口");
        System.out.println("  -sk, --secretKey <key>     认证密钥");
        System.out.println("  --help                     显示帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  etpc -c etpc.toml");
        System.out.println("  etpc -sk your-secret-key");
        System.out.println("  etpc -h localhost -p 9527 -sk your-secret-key");
        System.out.println("  etpc -host localhost -port 9527 -secretKey your-secret-key");
        System.out.println();
    }
}
