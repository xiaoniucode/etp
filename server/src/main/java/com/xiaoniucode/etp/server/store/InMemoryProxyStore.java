package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProxyStore implements ProxyStore {
    /**
     * proxyId -->
     */
    private final Map<String, ProxyConfig> proxyStore = new ConcurrentHashMap<>();
    /**
     * clientId -->proxyId:config
     */
    private final Map<String, Map<String, ProxyConfig>> clientProxyIndex = new ConcurrentHashMap<>();
    /**
     * clientId --> proxyName:config
     */
    private final Map<String, Map<String, ProxyConfig>> clientProxyNameIndex = new ConcurrentHashMap<>();
    /**
     * domain --> config
     */
    private final Map<String, ProxyConfig> domainToConfigIndex = new ConcurrentHashMap<>();
    /**
     * port --> config
     */
    private final Map<Integer, ProxyConfig> portToConfigIndex = new ConcurrentHashMap<>();

    @Override
    public ProxyConfig add(ProxyConfig config) {
        ProxyConfig existing = proxyStore.putIfAbsent(config.getProxyId(), config);
        if (existing != null) {
            return existing;
        }
        String clientId = config.getClientId();
        clientProxyIndex.computeIfAbsent(clientId, k -> new ConcurrentHashMap<>()).put(config.getProxyId(), config);
        if (config.isHttp() && config.getDomainInfo() != null && config.getDomainInfo().hasFullDomains()) {
            for (String domain : config.getDomainInfo().getFullDomains()) {
                domainToConfigIndex.put(domain, config);
            }
        }

        if (config.isTcp() && config.getRemotePort() != null) {
            portToConfigIndex.put(config.getRemotePort(), config);
        }
        clientProxyNameIndex.computeIfAbsent(clientId, k -> new ConcurrentHashMap<>()).put(config.getName(), config);
        return config;
    }

    @Override
    public ProxyConfig findById(String proxyId) {
        return proxyStore.get(proxyId);
    }

    @Override
    public List<ProxyConfig> findByClientId(String clientId) {
        Map<String, ProxyConfig> clientProxies = clientProxyIndex.get(clientId);
        if (clientProxies == null) {
            return List.of();
        }
        return new ArrayList<>(clientProxies.values());
    }

    @Override
    public ProxyConfig findByDomain(String domain) {
        return domainToConfigIndex.get(domain);
    }

    @Override
    public ProxyConfig findByRemotePort(Integer remotePort) {
        return portToConfigIndex.get(remotePort);
    }

    @Override
    public List<ProxyConfig> findAll() {
        return new ArrayList<>(proxyStore.values());
    }

    @Override
    public List<ProxyConfig> findAllHttpProxies() {
        List<ProxyConfig> httpProxies = new ArrayList<>();
        for (ProxyConfig config : proxyStore.values()) {
            if (config.isHttp()) {
                httpProxies.add(config);
            }
        }
        return httpProxies;
    }

    @Override
    public List<ProxyConfig> findAllTcpProxies() {
        List<ProxyConfig> tcpProxies = new ArrayList<>();
        for (ProxyConfig config : proxyStore.values()) {
            if (config.isTcp()) {
                tcpProxies.add(config);
            }
        }
        return tcpProxies;
    }

    @Override
    public void deleteById(String proxyId) {
        ProxyConfig config = proxyStore.remove(proxyId);
        if (config == null) {
            return;
        }
        if (config.getClientId() != null) {
            Map<String, ProxyConfig> clientProxies = clientProxyIndex.get(config.getClientId());
            if (clientProxies != null) {
                clientProxies.remove(proxyId);
                if (clientProxies.isEmpty()) {
                    clientProxyIndex.remove(config.getClientId());
                }
            }
            Map<String, ProxyConfig> clientProxyNameMap = clientProxyNameIndex.get(config.getClientId());
            if (clientProxyNameMap != null) {
                clientProxyNameMap.remove(proxyId);
                if (clientProxyNameMap.isEmpty()) {
                    clientProxyNameIndex.remove(config.getClientId());
                }
            }
        }
        if (config.isHttp() && config.getDomainInfo() != null && config.getDomainInfo().hasFullDomains()) {
            for (String domain : config.getDomainInfo().getFullDomains()) {
                domainToConfigIndex.remove(domain);
            }
        }

        if (config.isTcp() && config.getRemotePort() != null) {
            portToConfigIndex.remove(config.getRemotePort());
        }
    }

    @Override
    public void deleteByClientId(String clientId) {
        Map<String, ProxyConfig> clientProxies = clientProxyIndex.remove(clientId);
        if (clientProxies == null) {
            return;
        }
        for (ProxyConfig config : clientProxies.values()) {
            proxyStore.remove(config.getProxyId());
            if (config.isHttp() && config.getDomainInfo() != null && config.getDomainInfo().hasFullDomains()) {
                for (String domain : config.getDomainInfo().getFullDomains()) {
                    domainToConfigIndex.remove(domain);
                }
            }

            if (config.isTcp() && config.getRemotePort() != null) {
                portToConfigIndex.remove(config.getRemotePort());
            }
        }
        clientProxyNameIndex.remove(clientId);
    }

    @Override
    public boolean existsById(String proxyId) {
        return proxyStore.containsKey(proxyId);
    }

    @Override
    public ProxyConfig findByClientIdAndName(String clientId, String proxyName) {
        Map<String, ProxyConfig> clientProxyMap = clientProxyNameIndex.get(clientId);
        if (clientProxyMap != null) {
            return clientProxyMap.get(proxyName);
        }
        return null;
    }
}
