package cn.xilio.etp.client;

/**
 * 用于获取当前操作系统信息
 *
 * @author liuxin
 */
public class OSUtils {

    /**
     * 获取当前操作系统名称
     * 返回示例：Windows、macOS、Linux
     */
    public static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("mac")) {
            return "macOS";
        } else if (os.contains("linux")) {
            return "Linux";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "Unix";
        } else {
            return "Other";
        }
    }
}
