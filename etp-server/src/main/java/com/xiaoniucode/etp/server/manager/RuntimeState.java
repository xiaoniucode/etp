package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.config.ClientInfo;
import com.xiaoniucode.etp.server.config.ProxyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于统一管理运行时各种配置信息
 *
 * @author liuxin
 */
public final class RuntimeState {
    private final Logger logger = LoggerFactory.getLogger(RuntimeState.class);
    private static final RuntimeState INSTANCE = new RuntimeState();

    private RuntimeState() {
    }

    public static RuntimeState get() {
        return INSTANCE;
    }

    /**
     * secretKey -> 客户端运行时信息
     */
    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    /**
     * 公网端口 -> 内网端口
     */
    private final Map<Integer, Integer> portMapping = new ConcurrentHashMap<>();
    /**
     * secretKey -> remotePorts 用于客户端与其所有公网端口的映射，实现快速查找
     */
    private final Map<String, List<Integer>> clientRemotePorts = new ConcurrentHashMap<>();

    /**
     * 注册客户端
     * secretKey不能为空，否则无法注册
     */
    public void registerClient(ClientInfo client) {
        if (clients.containsKey(client.getSecretKey())) {
            logger.warn("改客户端已经被注册！");
            return;
        }
        clients.putIfAbsent(client.getSecretKey(), client);
        clientRemotePorts.put(client.getSecretKey(), new ArrayList<>());
    }

    public void updateClientName(String secretKey, String name) {
        ClientInfo client = clients.get(secretKey);
        if (client != null) {
            client.setName(name);
        }
    }

    /**
     * 用于判断客户端是否存在与系统中，在认证授权的时候会用到
     *
     * @param secretKey 认证密钥
     * @return 是否存在
     */
    public boolean hasClient(String secretKey) {
        return clients.containsKey(secretKey);
    }

    /**
     * 下线客户端
     */
    public void removeClient(String secretKey) {
        clients.remove(secretKey);
    }

    /**
     * 通过客户端密钥获取客户端信息
     */
    public ClientInfo getClient(String secretKey) {
        return clients.get(secretKey);
    }

    /**
     * 获取所有客户端信息
     */
    public Collection<ClientInfo> allClients() {
        return clients.values();
    }

    public List<Integer> getClientRemotePorts(String secretKey) {
        return clientRemotePorts.getOrDefault(secretKey, new ArrayList<>());
    }

    /**
     * 通过公网端口获取内网端口
     *
     * @param remotePort 公网端口
     * @return 如果没找到返回空
     */
    public int getLocalPort(Integer remotePort) {
        return portMapping.get(remotePort);
    }

    /**
     * 检查端口是否被占用
     *
     * @param remotePort 公网端口
     * @return 如果被占用返回true
     */
    public boolean isPortOccupied(int remotePort) {
        return portMapping.containsKey(remotePort);
    }

    /**
     * 将端口映射注册到指定的客户端
     *
     * @param secretKey 客户端密钥
     * @param proxy     端口映射信息
     */
    public boolean registerProxy(String secretKey, ProxyMapping proxy) {
        ClientInfo client = clients.get(secretKey);
        if (!Objects.isNull(client) && !isPortOccupied(proxy.getRemotePort())) {
             client.getProxies().add(proxy);
            clientRemotePorts.getOrDefault(secretKey, new ArrayList<>()).add(proxy.getRemotePort());
            portMapping.put(proxy.getRemotePort(), proxy.getLocalPort());
            logger.info("映射{} - {} 注册成功", proxy.getName(), proxy.getRemotePort());
            return true;
        }
        return false;
    }

    /**
     * 判断时候已经注册了相同的端口映射
     *
     * @param secretKey  客户端密钥
     * @param remotePort 公网端口
     * @return 是否已经注册
     */
    public boolean hasProxy(String secretKey, Integer remotePort) {
        List<Integer> remotePorts = clientRemotePorts.get(secretKey);
        return remotePorts.contains(remotePort);
    }

    /**
     * 根据客户端密钥和公网端口删除端口映射信息
     *
     * @param secretKey  客户端密钥
     * @param remotePort 公网端口
     * @return 是否删除成功
     */
    public boolean removeProxy(String secretKey, Integer remotePort) {
        ClientInfo client = clients.get(secretKey);
        if (!Objects.isNull(client)) {
            List<ProxyMapping> proxies = client.getProxies();
            proxies.removeIf(proxy -> proxy.getRemotePort().equals(remotePort));
            clientRemotePorts.getOrDefault(secretKey, new ArrayList<>()).remove(remotePort);
            portMapping.remove(remotePort);
        }
        return false;
    }

    /**
     * 更新端口映射的状态
     *
     * @param secretKey  客户端密钥
     * @param remotePort 公网端口
     * @param status     更新状态值
     * @return 是否更新成功
     */
    public boolean updateProxyStatus(String secretKey, Integer remotePort, Integer status) {
        ClientInfo client = clients.get(secretKey);
        if (!Objects.isNull(client)) {
            List<ProxyMapping> proxies = client.getProxies();
            for (ProxyMapping proxy : proxies) {
                if (proxy.getRemotePort().equals(remotePort)) {
                    proxy.setStatus(status);
                    return true;
                }
            }
        }
        return false;
    }
}
