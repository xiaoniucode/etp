package com.xiaoniucode.etp.client.common.utils;

/**
 * 获取当前操作系统相关的系统信息
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
    /**
     * 获取系统架构
     */
    public static String getOSArch() {
        return System.getProperty("os.arch");
    }
    public static String getUsername() {
        return System.getProperty("user.name");
    }
}
