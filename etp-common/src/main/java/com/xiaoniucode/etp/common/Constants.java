package com.xiaoniucode.etp.common;

import java.io.File;

/**
 * 常量
 *
 * @author liuxin
 */
public class Constants {
    public static final String SQLITE_DB_URL = "jdbc:sqlite:etp.db";
    public static final String LOG_BASE_PATH = "logs" + File.separator;

    public static final String DEFAULT_CLIENT_CONFIG_NAME = "etpc.toml";
    public static final String DEFAULT_CLIENT_LOG_PATTERN = "etpc.%d{yyyy-MM-dd}.log.gz";
    public static final String DEFAULT_CLIENT_LOG_NAME = "etpc.log";

    public static final String DEFAULT_SERVER_CONFIG_NAME = "etps.toml";
    public static final String DEFAULT_SERVER_LOG_PATTERN = "etps.%d{yyyy-MM-dd}.log.gz";
    public static final String DEFAULT_SERVER_LOG_NAME = "etps.log";
}
