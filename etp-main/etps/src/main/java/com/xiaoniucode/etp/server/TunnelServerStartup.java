package com.xiaoniucode.etp.server;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.common.*;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.common.log.LogbackConfigurator;
import com.xiaoniucode.etp.core.event.GlobalEventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.config.TomlConfigSource;
import com.xiaoniucode.etp.server.event.DatabaseInitEvent;
import com.xiaoniucode.etp.server.event.TunnelBindEvent;
import com.xiaoniucode.etp.server.listener.DatabaseInitListener;
import com.xiaoniucode.etp.server.listener.StaticConfigInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 服务启动入口
 *
 * @author liuxin
 */
public class TunnelServerStartup {
    private static final Logger logger = LoggerFactory.getLogger(TunnelServerStartup.class);

    static {
        System.setProperty("io.netty.leakDetection.level", "DISABLED");
    }

    public static void main(String[] args) {
        try {
            AppConfig config = buildConfig(args);
            ConfigHelper.set(config);
            initLogback(config);
            int bindPort = config.getBindPort();
            if (PortChecker.isPortOccupied(bindPort)) {
                logger.error("{} 端口已经被占用", bindPort);
                return;
            }
            GlobalEventBus.get().subscribe(TunnelBindEvent.class, new DatabaseInitListener());
            GlobalEventBus.get().subscribe(DatabaseInitEvent.class, new StaticConfigInitListener());

            TunnelServer tunnelServer = new TunnelServer(config);
            registerShutdownHook(tunnelServer);
            tunnelServer.start();

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
        cmdArgs.registerOption("help", "h", "显示帮助信息", false, false);

        try {
            cmdArgs.parse(args);

            if (cmdArgs.has("help")) {
                printHelp();
                System.exit(0);
            }

            if (cmdArgs.has("config")) {
                return loadConfigFromFile(cmdArgs.get("config"));
            }
            return AppConfig.builder().build();

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

    private static void printHelp() {
        System.out.println("用法: etps [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  -c, --config <path>         配置文件路径");
        System.out.println("  --help                      显示帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  etps -c etps.toml");
        System.out.println("  etps");
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

    private static void registerShutdownHook(TunnelServer tunnelServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (tunnelServer != null) {
                tunnelServer.stop();
            }
        }));
    }
}
