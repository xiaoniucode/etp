package com.xiaoniucode.etp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 配置工具类
 *
 * @author liuxin
 */
public class ConfigUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static String getConfigPath(String[] args, String fileName) {
        if (args.length == 0) {
            Path currentDir = Paths.get("").toAbsolutePath();
            Path defaultConfig = currentDir.resolve(fileName);

            if (Files.exists(defaultConfig)) {
                return defaultConfig.toString();
            }
        } else {
            if (args.length < 2) {
                logger.error("请指定配置文件！");
                return null;
            }
            if (!"-c".equalsIgnoreCase(args[0])) {
                logger.error("参数有误，请通过-c <path>指定配置文件！");
                return null;
            }
            if (args[1] == null) {
                logger.error("配置文件路径不能为空！");
                return null;
            }
            if (!args[1].endsWith(".toml")) {
                logger.error("配置文件格式有误，请使用xxx.toml格式！");
                return null;
            }
            if (!new File(args[1]).exists()) {
                logger.error("配置文件不存在，请检查路径是否正确！");
                return null;
            }
            return args[1];
        }
        return null;
    }
}
