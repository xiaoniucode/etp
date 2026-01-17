package com.xiaoniucode.etp.server.factory;

import com.xiaoniucode.etp.server.manager.port.PortManager;
import com.xiaoniucode.etp.server.manager.port.PortRange;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 端口管理器工厂
 * 负责创建和管理多个PortManager实例
 * 支持TCP和UDP端口管理器
 */
public class PortManagerFactory {
    private static final PortManagerFactory INSTANCE = new PortManagerFactory();

    /**
     * tcp:0.0.0.0 or udp:0.0.0.0
     */
    private final Map<String, PortManager> portManagers;


    private PortManagerFactory() {
        this.portManagers = new ConcurrentHashMap<>();
    }
    public static PortManagerFactory get() {
        return INSTANCE;
    }

    /**
     * 创建或获取TCP端口管理器实例
     *
     * @param bindAddr   绑定地址
     * @param allowPorts 允许使用的端口范围
     * @return TCP端口管理器实例
     */
    public PortManager createTcpPortManager(String bindAddr, List<PortRange> allowPorts) {
        String key = buildKey("tcp", bindAddr);
        return portManagers.computeIfAbsent(key, k -> new PortManager("tcp", bindAddr, allowPorts));
    }

    /**
     * 创建或获取UDP端口管理器实例
     *
     * @param bindAddr   绑定地址
     * @param allowPorts 允许使用的端口范围
     * @return UDP端口管理器实例
     */
    public PortManager createUdpPortManager(String bindAddr, List<PortRange> allowPorts) {
        String key = buildKey("udp", bindAddr);
        return portManagers.computeIfAbsent(key, k -> new PortManager("udp", bindAddr, allowPorts));
    }

    /**
     * 获取已存在的TCP端口管理器实例
     *
     * @param bindAddr 绑定地址
     * @return TCP端口管理器实例，不存在则返回null
     */
    public PortManager getTcpPortManager(String bindAddr) {
        String key = buildKey("tcp", bindAddr);
        return portManagers.get(key);
    }

    /**
     * 获取已存在的UDP端口管理器实例
     *
     * @param bindAddr 绑定地址
     * @return UDP端口管理器实例，不存在则返回null
     */
    public PortManager getUdpPortManager(String bindAddr) {
        String key = buildKey("udp", bindAddr);
        return portManagers.get(key);
    }

    /**
     * 删除指定的TCP端口管理器实例
     *
     * @param bindAddr 绑定地址
     */
    public void removeTcpPortManager(String bindAddr) {
        String key = buildKey("tcp", bindAddr);
        portManagers.remove(key);
    }

    /**
     * 删除指定的UDP端口管理器实例
     *
     * @param bindAddr 绑定地址
     */
    public void removeUdpPortManager(String bindAddr) {
        String key = buildKey("udp", bindAddr);
        portManagers.remove(key);
    }

    /**
     * 清除所有端口管理器实例
     */
    public void clearAllPortManagers() {
        portManagers.clear();
    }

    /**
     * 构建实例缓存键
     */
    private String buildKey(String type, String bindAddr) {
        return type + ":" + bindAddr;
    }
}