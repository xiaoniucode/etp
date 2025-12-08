package com.xiaoniucode.etp.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Logback 配置类
 * @author liuxin
 */
public class LogbackConfigurator {

    private final LogbackConfig config;

    /**
     * 私有构造函数，通过建造者创建实例
     */
    private LogbackConfigurator(LogbackConfig config) {
        this.config = config;
    }
    /**
     * 日志配置的不可变对象
     */
    public static class LogbackConfig {
        private final String logDir;
        private final String logFilePath;
        private final String archiveFilePattern;
        private final String logPattern;
        private final int maxHistory;
        private final String totalSizeCap;
        private final Level logLevel;

        private LogbackConfig(String logDir, String logFilePath, String archiveFilePattern, String logPattern,
                             int maxHistory, String totalSizeCap, Level logLevel) {
            this.logDir = logDir;
            this.logFilePath = logFilePath;
            this.archiveFilePattern = archiveFilePattern;
            this.logPattern = logPattern;
            this.maxHistory = maxHistory;
            this.totalSizeCap = totalSizeCap;
            this.logLevel = logLevel;
        }
        public String getLogDir() { return logDir; }
        public String getLogFilePath() { return logFilePath; }
        public String getArchiveFilePattern() { return archiveFilePattern; }
        public String getLogPattern() { return logPattern; }
        public int getMaxHistory() { return maxHistory; }
        public String getTotalSizeCap() { return totalSizeCap; }
        public Level getLogLevel() { return logLevel; }
    }

    /**
     * 建造者类，用于构建 Logback 配置
     */
    public static class LogbackConfigBuilder {
        private String logDir = "logs";
        private String logFilePath = logDir + File.separator + "app.log";
        private String archiveFilePattern = logDir + File.separator + "app.%d{yyyy-MM-dd}.log";
        private String logPattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
        private int maxHistory = 30;
        private String totalSizeCap = "3GB";
        private Level logLevel = Level.INFO;

        public LogbackConfigBuilder setLogDir(String logDir) {
            this.logDir = logDir;
            return this;
        }

        public LogbackConfigBuilder setLogFilePath(String logFilePath) {
            this.logFilePath = logFilePath;
            return this;
        }

        public LogbackConfigBuilder setArchiveFilePattern(String archiveFilePattern) {
            this.archiveFilePattern = archiveFilePattern;
            return this;
        }

        public LogbackConfigBuilder setLogPattern(String logPattern) {
            this.logPattern = logPattern;
            return this;
        }

        public LogbackConfigBuilder setMaxHistory(int maxHistory) {
            this.maxHistory = maxHistory;
            return this;
        }

        public LogbackConfigBuilder setTotalSizeCap(String totalSizeCap) {
            this.totalSizeCap = totalSizeCap;
            return this;
        }

        public LogbackConfigBuilder setLogLevel(Level logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public LogbackConfigurator build() {
            LogbackConfig config = new LogbackConfig(logDir, logFilePath, archiveFilePattern, logPattern,
                    maxHistory, totalSizeCap, logLevel);
            return new LogbackConfigurator(config);
        }
    }

    /**
     * 配置 Logback 日志系统
     */
    public void configureLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        // 配置编码器
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(config.getLogPattern());
        encoder.start();

        // 配置控制台输出
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("Console");
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        // 创建日志目录
        File logDirectory = new File(config.getLogDir());
        if (!logDirectory.exists()) {
            boolean created = logDirectory.mkdirs();
            if (!created) {
                System.err.println("无法创建日志目录: " + config.getLogDir());
            }
        }

        // 配置文件输出
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("File");
        fileAppender.setFile(config.getLogFilePath());

        // 配置按天滚动的策略
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(config.getArchiveFilePattern());
        rollingPolicy.setMaxHistory(config.getMaxHistory());
        rollingPolicy.setTotalSizeCap(FileSize.valueOf(config.getTotalSizeCap()));
        rollingPolicy.start();

        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setEncoder(encoder);
        fileAppender.start();

        // 配置根 Logger
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(config.getLogLevel());
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);
    }
}
