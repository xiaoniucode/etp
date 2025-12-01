package cn.xilio.etp.server.store;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 运行时各种配置信息状态管理
 *
 * @author liuxin
 */
public final class RuntimeState {
    private static final RuntimeState INSTANCE = new RuntimeState();

    private RuntimeState() {
    }

    public static RuntimeState get() {
        return INSTANCE;
    }

    // secretKey -> 客户端运行时信息
    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    // 公网端口 -> 代理映射
    private final Map<Integer, ProxyMapping> portMapping = new ConcurrentHashMap<>();

    /**
     * 注册客户端
     * secretKey不能为空，否则无法注册
     */
    public void registerClient(ClientInfo client) {
        clients.putIfAbsent(client.getSecretKey(), client);
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
        return Collections.unmodifiableCollection(clients.values());
    }

    /**
     * 通过公网端口获取内网端口
     *
     * @param remotePort 公网端口
     * @return 如果没找到返回空
     */
    public Integer getLocalPort(Integer remotePort) {
        ProxyMapping proxyMapping = portMapping.get(remotePort);
        return Objects.isNull(proxyMapping) ? null : proxyMapping.getLocalPort();
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
        if (!Objects.isNull(client) && isPortOccupied(proxy.getRemotePort())) {
            client.getProxies().add(proxy);
            return true;
        }
        return false;
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
        }
        return false;
    }
}
