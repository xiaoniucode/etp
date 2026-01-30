package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.LanInfo;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class ProxyManager {
    private static final Logger logger = LoggerFactory.getLogger(ProxyManager.class);
    /**
     * remotePort --> ProxyConfig
     */
    private final Map<Integer, ProxyConfig> portToProxyConfig = new ConcurrentHashMap<>();
    /**
     * domain -->ProxyConfig
     */
    private final Map<String, ProxyConfig> domainToProxyConfig = new ConcurrentHashMap<>();
    /**
     * clientId --> ProxyConfigs
     */
    private final Map<String, Set<ProxyConfig>> clientIdToProxyConfigs = new ConcurrentHashMap<>();

    public ProxyConfig addProxy(String clientId, ProxyConfig proxyConfig, Consumer<ProxyConfig> callback) {
        ProtocolType protocol = proxyConfig.getProtocol();
        if (ProtocolType.isTcp(protocol)) {
            portToProxyConfig.put(proxyConfig.getRemotePort(), proxyConfig);
        }
        if (ProtocolType.isHttp(protocol)) {
            Set<String> fullDomains = proxyConfig.getFullDomains();
            for (String domain : fullDomains) {
                domainToProxyConfig.put(domain, proxyConfig);
            }
        }
        if (callback != null) {
            callback.accept(proxyConfig);
        }
        clientIdToProxyConfigs.computeIfAbsent(clientId, k ->
                ConcurrentHashMap.newKeySet()).add(proxyConfig);
        return null;
    }

    public boolean hasDomain(String domain) {
        return domainToProxyConfig.containsKey(domain);
    }

    public boolean hasRemotePort(Integer remotePort) {
        return portToProxyConfig.containsKey(remotePort);
    }

    public ProxyConfig getProxyConfigByRemotePort(int remotePort) {
        return portToProxyConfig.get(remotePort);
    }

    public ProxyConfig getProxyConfigByDomain(String domain) {
        return domainToProxyConfig.get(domain);
    }

    public LanInfo getLanInfoByRemotePort(Integer port) {
        return new LanInfo("localhost", 3306);
    }

    public LanInfo getLanInfoByDomain(String domain) {
        return new LanInfo("localhost", 8081);
    }

    /**
     * 获取某一个客户端所有的代理配置
     *
     * @param clientId 客户端 ID
     * @return 代理配置列表
     */
    public Set<ProxyConfig> getProxyConfigsByClientId(String clientId) {
        return clientIdToProxyConfigs.get(clientId);
    }
}
