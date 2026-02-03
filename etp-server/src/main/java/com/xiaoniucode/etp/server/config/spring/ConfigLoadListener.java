package com.xiaoniucode.etp.server.config.spring;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.common.CommandLineArgs;
import com.xiaoniucode.etp.common.PortChecker;
import com.xiaoniucode.etp.common.log.LogConfig;
import com.xiaoniucode.etp.common.log.LogbackConfigurator;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.TomlConfigSource;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 解析启动参数加载配置文件或解析命令
 */
public class ConfigLoadListener implements SpringApplicationRunListener {
    private final Logger logger = LoggerFactory.getLogger(ConfigLoadListener.class);
    private final String[] args;
    private AppConfig appConfig;

    public ConfigLoadListener(SpringApplication application, String[] args) {
        this.args = args;
    }

    @Override
    public void starting(@NonNull ConfigurableBootstrapContext bootstrapContext) {
        try {
            System.setProperty("io.netty.leakDetection.level", "DISABLED");
            appConfig = buildConfig(args);
            initLogback(appConfig);
            int bindPort = appConfig.getServerPort();
            if (PortChecker.isPortOccupied(bindPort)) {
                logger.error("{} 端口已经被占用", bindPort);
                System.exit(0);
            }
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            System.err.println("错误: " + e.getMessage());
            printHelp();
            System.exit(1);
        } catch (Exception e) {
            logger.error("启动失败", e);
            System.err.println("启动失败: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        if (appConfig!=null){
            logger.debug("注册配置类到IOC 容器");
            context.getBeanFactory().registerSingleton("appConfig", appConfig);
        }else {
            logger.warn("配置为 null");
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

    private static AppConfig loadConfigFromFile(String configPath) {
        if (!Files.exists(Paths.get(configPath))) {
            throw new IllegalArgumentException("配置文件不存在: " + configPath);
        }
        TomlConfigSource configSource = new TomlConfigSource(configPath);
        return configSource.load();
    }
}
