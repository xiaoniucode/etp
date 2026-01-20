package com.xiaoniucode.etp.server.manager.re;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyManager {
    private final Map<Integer, Integer> portMapping = new ConcurrentHashMap<>();
    private final Map<String, Integer> domainMapping = new ConcurrentHashMap<>();

    /**
     * 获取连接目标端口
     * @param remotePort 公网监听端口
     * @return 内网服务监听端口(localPort)
     */
    public static int getLocalPort(int remotePort) {
        return 0;
    }
}
