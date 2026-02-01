package com.xiaoniucode.etp.client.config;

public class ConfigUtils {
    private static AppConfig config;
    
    private ConfigUtils() {}
    
    public static void setConfig(AppConfig appConfig) {
        config = appConfig;
    }
    
    public static AppConfig getConfig() {
        if (config == null) {
            throw new IllegalStateException("Config not set");
        }
        return config;
    }
    
    public static boolean hasConfig() {
        return config != null;
    }
}