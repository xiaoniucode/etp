package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.LanInfo;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ProxyManager {
    private static final Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    /**
     * 公网端口 -> 内网信息
     */
    private static final Map<Integer, LanInfo> portMapping = new ConcurrentHashMap<>();

    /**
     * 域名 -> 内网端口
     */
    private static final Map<String, LanInfo> domainMapping = new ConcurrentHashMap<>();

    /**
     * secretKey -> remotePorts 用于客户端与其所有公网端口的映射，实现快速查找
     */
    private static final Map<String, Set<Integer>> clientRemotePorts = new ConcurrentHashMap<>();

    /**
     * secretKey -> customDomains 用于客户端与其所有域名的映射，实现快速查找
     */
    private static final Map<String, Set<String>> clientDomains = new ConcurrentHashMap<>();
    /**
     * remotePort|domain -> 代理状态
     */
    private static final Map<String, Integer> proxyStatus = new ConcurrentHashMap<>();
    /**
     * proxyId -> ProxyConfig
     */
    private static final Map<Integer, ProxyConfig> proxyConfigs = new ConcurrentHashMap<>();

    public static Set<Integer> getClientRemotePorts(String secretKey) {
        return clientRemotePorts.getOrDefault(secretKey, new HashSet<>());
    }

    public static LanInfo getLanInfo(int remotePort) {
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
        proxyConfigs.put(proxy.getProxyId(), proxy);
        Channel control = ChannelManager.getControl(secretKey);

        if (ProtocolType.isTcp(proxy.getType()) && !isPortOccupied(proxy.getRemotePort())) {
            clientRemotePorts.computeIfAbsent(secretKey, k -> new CopyOnWriteArraySet<>()).add(proxy.getRemotePort());
            portMapping.put(proxy.getRemotePort(), new LanInfo(proxy.getLocalIP(), proxy.getLocalPort()));
            proxyStatus.put(proxy.getRemotePort() + "", proxy.getStatus());
            logger.debug("TCP代理 {} 注册成功", proxy.getName());
            return true;
        }

        if (ProtocolType.isHttpOrHttps(proxy.getType())) {
            Set<String> domains = DomainManager.addDomainsSmartly(proxy);
            clientDomains.computeIfAbsent(secretKey, k -> new CopyOnWriteArraySet<>()).addAll(domains);
            domains.forEach(domain -> {
                domainMapping.put(domain, new LanInfo(proxy.getLocalIP(), proxy.getLocalPort()));
                proxyStatus.put(domain, proxy.getStatus());
                ChannelManager.addDomainToControl(domain,control);
            });
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

    public static LanInfo getLanInfoByDomain(String domain) {
        if (!StringUtils.hasText(domain)) {
            throw new IllegalArgumentException("domain is empty");
        }
        return domainMapping.get(domain);
    }

    public static int getProxyStatus(String domain) {
        return proxyStatus.getOrDefault(domain, -1);
    }

    public static ProxyConfig getProxyConfig(Integer proxyId) {
        return proxyConfigs.get(proxyId);
    }
}
