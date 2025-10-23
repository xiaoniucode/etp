package cn.xilio.etp.common;

import cn.xilio.etp.common.ansi.AnsiLog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author liuxin
 */
public class ConfigUtils {
    public static String getConfigPath(String[] args,String fileName) {
        if (args.length == 0) {
            Path currentDir = Paths.get("").toAbsolutePath();
            Path defaultConfig = currentDir.resolve(fileName);

            if (Files.exists(defaultConfig)) {
                return defaultConfig.toString();
            }
        } else {
            if (args.length < 2) {
                AnsiLog.error("请指定配置文件！");
                return null;
            }
            if (!"-c".equalsIgnoreCase(args[0])) {
                AnsiLog.error("参数有误，请通过-c <path>指定配置文件！");
                return null;
            }
            if (args[1] == null) {
                AnsiLog.error("配置文件路径不能为空！");
                return null;
            }
            if (!args[1].endsWith(".toml")) {
                AnsiLog.error("配置文件格式有误，请使用xxx.toml格式！");
                return null;
            }
            if (!new File(args[1]).exists()) {
                AnsiLog.error("配置文件不存在，请检查路径是否正确！");
                return null;
            }
            return args[1];
        }
        return null;
    }
}
