package com.xiaoniucode.etp.common;

import ch.qos.logback.classic.Level;

/**
 * 系统常量
 *
 * @author liuxin
 */
public class Constants {
    public static final String SQLITE_DB_URL = "jdbc:sqlite:etp.db";
    public static final Level LOG_LEVEL = Level.INFO;
    public static final String LOG_BASE_PATH = "logs";

    public static final int LOG_MAX_HISTORY = 30;
    public static final String LOG_TOTAL_SIZE_CAP = "3GB";

    public static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";

    public static final String CLIENT_CONFIG_NAME = "etpc.toml";
    public static final String CLIENT_LOG_ARCHIVE_PATTERN = "etpc.%d{yyyy-MM-dd}.log.gz";
    public static final String CLIENT_LOG_NAME = "etpc.log";

    public static final String SERVER_CONFIG_NAME = "etps.toml";
    public static final String SERVER_LOG_ARCHIVE_PATTERN = "etps.%d{yyyy-MM-dd}.log.gz";
    public static final String SERVER_LOG_NAME = "etps.log";

}
