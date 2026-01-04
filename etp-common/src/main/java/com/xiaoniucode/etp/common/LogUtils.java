package com.xiaoniucode.etp.common;

import ch.qos.logback.classic.Level;
import com.moandjiezana.toml.Toml;
import java.io.File;

/**
 * 日志工具类
 * @author liuxin
 */
public class LogUtils {
    public static LogConfig createServerDefaultConfig() {
        return new LogConfig(
            Constants.LOG_BASE_PATH,
            Constants.SERVER_LOG_NAME,
            Constants.SERVER_LOG_ARCHIVE_PATTERN,
            Constants.LOG_PATTERN,
            Constants.LOG_MAX_HISTORY,
            Constants.LOG_TOTAL_SIZE_CAP,
            Constants.LOG_LEVEL
        );
    }

    public static LogConfig createClientDefaultConfig() {
        return new LogConfig(
            Constants.LOG_BASE_PATH,
            Constants.CLIENT_LOG_NAME,
            Constants.CLIENT_LOG_ARCHIVE_PATTERN,
            Constants.LOG_PATTERN,
            Constants.LOG_MAX_HISTORY,
            Constants.LOG_TOTAL_SIZE_CAP,
            Constants.LOG_LEVEL
        );
    }

    /**
     * 创建日志配置
     *
     * @param log      toml配置文件
     * @param isServer 是否是服务端配置
     * @return 配置
     */
    public static LogConfig parseLogConfig(Toml log, boolean isServer) {
        LogConfig logConfig = isServer ? createServerDefaultConfig() : createClientDefaultConfig();
        if (log != null) {
            String level = log.getString("level");
            if (StringUtils.hasText(level)) {
                logConfig.setLevel(Level.toLevel(level, Constants.LOG_LEVEL));
            }

            String path = log.getString("path");
            if (StringUtils.hasText(path)) {
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                logConfig.setPath(path);
            }

            String name = log.getString("name");
            if (StringUtils.hasText(name)) {
                logConfig.setName(name);
            }

            String archivePattern = log.getString("archivePattern");
            if (StringUtils.hasText(archivePattern)) {
                logConfig.setArchivePattern(archivePattern);
            }

            String pattern = log.getString("logPattern");
            if (StringUtils.hasText(pattern)) {
                logConfig.setLogPattern(pattern);
            } else {
                pattern = log.getString("pattern");
                if (StringUtils.hasText(pattern)) {
                    logConfig.setLogPattern(pattern);
                }
            }

            Long maxHistory = log.getLong("maxHistory");
            if (maxHistory != null) {
                logConfig.setMaxHistory(maxHistory.intValue());
            }

            String totalSizeCap = log.getString("totalSizeCap");
            if (StringUtils.hasText(totalSizeCap)) {
                logConfig.setTotalSizeCap(totalSizeCap);
            }
        }
        return logConfig;
    }
}
