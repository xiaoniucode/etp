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
     * agentId -->proxyId:config
     */
    private final Map<String, Map<String, ProxyConfig>> clientProxyIndex = new ConcurrentHashMap<>();
    /**
     * agentId --> proxyName:config
     */
    private final Map<String, Map<String, ProxyConfig>> clientProxyNameIndex = new ConcurrentHashMap<>();
    /**
     * port --> config
     */
    private final Map<Integer, ProxyConfig> portToConfigIndex = new ConcurrentHashMap<>();

    @Override
    public ProxyConfig save(ProxyConfig config) {
        ProxyConfig existing = proxyStore.putIfAbsent(config.getProxyId(), config);
        if (existing != null) {
            return existing;
        }
        String agentId = config.getAgentId();
        clientProxyIndex.computeIfAbsent(agentId, k -> new ConcurrentHashMap<>()).put(config.getProxyId(), config);

        if (config.isTcp() && config.getListenPort() != null) {
            portToConfigIndex.put(config.getListenPort(), config);
        }
        clientProxyNameIndex.computeIfAbsent(agentId, k -> new ConcurrentHashMap<>()).put(config.getName(), config);
        return config;
    }

    @Override
    public boolean replace(ProxyConfig proxyConfig) {
        deleteById(proxyConfig.getProxyId());
        save(proxyConfig);
        return true;
    }

    @Override
    public ProxyConfig findById(String proxyId) {
        return proxyStore.get(proxyId);
    }

    @Override
    public List<ProxyConfig> findByAgentId(String agentId) {
        Map<String, ProxyConfig> clientProxies = clientProxyIndex.get(agentId);
        if (clientProxies == null) {
            return List.of();
        }
        return new ArrayList<>(clientProxies.values());
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
        if (config.getAgentId() != null) {
            Map<String, ProxyConfig> clientProxies = clientProxyIndex.get(config.getAgentId());
            if (clientProxies != null) {
                clientProxies.remove(proxyId);
                if (clientProxies.isEmpty()) {
                    clientProxyIndex.remove(config.getAgentId());
                }
            }
            Map<String, ProxyConfig> clientProxyNameMap = clientProxyNameIndex.get(config.getAgentId());
            if (clientProxyNameMap != null) {
                clientProxyNameMap.remove(proxyId);
                if (clientProxyNameMap.isEmpty()) {
                    clientProxyNameIndex.remove(config.getAgentId());
                }
            }
        }
        if (config.isTcp() && config.getListenPort() != null) {
            portToConfigIndex.remove(config.getListenPort());
        }
    }

    @Override
    public void deleteByAgentId(String agentId) {
        Map<String, ProxyConfig> clientProxies = clientProxyIndex.remove(agentId);
        if (clientProxies == null) {
            return;
        }
        for (ProxyConfig config : clientProxies.values()) {
            proxyStore.remove(config.getProxyId());
            if (config.isTcp() && config.getListenPort() != null) {
                portToConfigIndex.remove(config.getListenPort());
            }
        }
        clientProxyNameIndex.remove(agentId);
    }

    @Override
    public boolean existsById(String proxyId) {
        return proxyStore.containsKey(proxyId);
    }

    @Override
    public ProxyConfig findByAgentIdAndName(String agentId, String proxyName) {
        Map<String, ProxyConfig> clientProxyMap = clientProxyNameIndex.get(agentId);
        if (clientProxyMap != null) {
            return clientProxyMap.get(proxyName);
        }
        return null;
    }
}
