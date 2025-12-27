package com.xiaoniucode.etp.common;

import ch.qos.logback.classic.Level;

/**
 * 日志配置
 *
 * @author liuxin
 */
public class LogConfig {
    private String path;
    private String name;
    private String archivePattern;
    private String logPattern;
    private int maxHistory;
    private String totalSizeCap;
    private Level level;

    public LogConfig(String path, String name, String archivePattern, String logPattern, int maxHistory,
        String totalSizeCap, Level level) {
        this.path = path;
        this.name = name;
        this.archivePattern = archivePattern;
        this.logPattern = logPattern;
        this.maxHistory = maxHistory;
        this.totalSizeCap = totalSizeCap;
        this.level = level;
    }

    public LogConfig() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArchivePattern() {
        return archivePattern;
    }

    public void setArchivePattern(String archivePattern) {
        this.archivePattern = archivePattern;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public String getTotalSizeCap() {
        return totalSizeCap;
    }

    public void setTotalSizeCap(String totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
