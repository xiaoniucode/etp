package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class ProxyManager {
    private static final Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    /**
     * 公网端口 -> 内网端口
     */
    private static final Map<Integer, Integer> portMapping = new ConcurrentHashMap<>();

    /**
     * 域名 -> 内网端口
     */
    private static final Map<String, Integer> domainMapping = new ConcurrentHashMap<>();

    /**
     * secretKey -> remotePorts 用于客户端与其所有公网端口的映射，实现快速查找
     */
    private static final Map<String, Set<Integer>> clientRemotePorts = new ConcurrentHashMap<>();

    /**
     * secretKey -> domains 用于客户端与其所有域名的映射，实现快速查找
     */
    private static final Map<String, Set<String>> clientDomains = new ConcurrentHashMap<>();

    public static Set<Integer> getClientRemotePorts(String secretKey) {
        return clientRemotePorts.getOrDefault(secretKey, new HashSet<>());
    }

    public static int getLocalPort(int remotePort) {
        return portMapping.get(remotePort);
    }

    public static boolean isPortOccupied(int remotePort) {
        return portMapping.containsKey(remotePort);
    }

    public static boolean addProxy(String secretKey, ProxyConfig proxy) {
        ClientInfo client = ClientManager.getClient(secretKey);
        if (client == null) {
            return false;
        }
        client.getProxies().add(proxy);
        if (proxy.getType() == ProtocolType.TCP && !isPortOccupied(proxy.getRemotePort())) {
            clientRemotePorts.computeIfAbsent(secretKey, k -> new CopyOnWriteArraySet<>()).add(proxy.getRemotePort());
            portMapping.put(proxy.getRemotePort(), proxy.getLocalPort());
            logger.debug("TCP代理 {} 注册成功", proxy.getName());
            return true;
        }

        if (proxy.getType() == ProtocolType.HTTP) {
            Set<String> domains = proxy.getDomains();
            clientDomains.computeIfAbsent(secretKey, k -> new CopyOnWriteArraySet<>()).addAll(domains);
            domains.forEach(domain -> domainMapping.put(domain, proxy.getLocalPort()));
            logger.debug("HTTP代理 {} 注册成功", proxy.getName());
            return true;
        }

        return false;
    }

    public static boolean hasProxy(String secretKey, Integer remotePort) {
        Set<Integer> remotePorts = clientRemotePorts.get(secretKey);
        return remotePorts != null && remotePorts.contains(remotePort);
    }

    public static boolean removeProxy(String secretKey, Integer remotePort) {
        ClientInfo client = ClientManager.getClient(secretKey);
        if (!Objects.isNull(client)) {
            List<ProxyConfig> proxies = client.getTcpProxies();
            proxies.removeIf(proxy -> proxy.getRemotePort().equals(remotePort));

            Set<Integer> ports = clientRemotePorts.get(secretKey);
            if (ports != null) {
                ports.remove(remotePort);
            }

            portMapping.remove(remotePort);
            return true;
        }

        return false;
    }

    public static boolean updateProxyStatus(String secretKey, Integer remotePort, Integer status) {
        ClientInfo client = ClientManager.getClient(secretKey);
        if (!Objects.isNull(client)) {
            List<ProxyConfig> proxies = client.getTcpProxies();
            for (ProxyConfig proxy : proxies) {
                if (proxy.getRemotePort().equals(remotePort)) {
                    proxy.setStatus(status);
                    return true;
                }
            }
        }

        return false;
    }

    public static Set<String> getClientDomains(String secretKey) {
        return clientDomains.getOrDefault(secretKey, new HashSet<>());
    }

    public static int getLocalPortByDomain(String domain) {
        if (!StringUtils.hasText(domain)) {
            throw new IllegalArgumentException("domain is empty");
        }
        return domainMapping.getOrDefault(domain, -1);
    }
}
