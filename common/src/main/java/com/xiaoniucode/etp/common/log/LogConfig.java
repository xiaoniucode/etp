package com.xiaoniucode.etp.common.log;

import ch.qos.logback.classic.Level;
import lombok.Getter;
import lombok.Setter;

/**
 * 日志配置
 *
 * @author liuxin
 */
@Getter
@Setter
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
}
