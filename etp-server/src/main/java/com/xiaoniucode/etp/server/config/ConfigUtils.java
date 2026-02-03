package com.xiaoniucode.etp.server.config;

public class ConfigUtils {
    private static volatile AppConfig instance;

    private ConfigUtils() {
    }

    public static void set(AppConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置不能为null");
        }
        instance = config;
    }

    public static AppConfig get() {
        if (instance == null) {
            throw new IllegalStateException("配置未初始化");
        }
        return instance;
    }
}
