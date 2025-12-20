package com.xiaoniucode.etp.server.web.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取IP工具类
 *
 * @author liuxin
 */
public class IpUtils {
    /**
     * 获取IP地址
     *
     * @return 本地IP地址
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ignored) {
        }
        return "127.0.0.1";
    }

    /**
     * 获取主机名
     *
     * @return 本地主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
        }
        return "未知";
    }
}
