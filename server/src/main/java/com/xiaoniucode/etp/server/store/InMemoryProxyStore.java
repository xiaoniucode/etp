package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import org.springframework.stereotype.Repository;

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
     * clientId -->
     */
    private final Map<String, Map<String, ProxyConfig>> clientProxyIndex = new ConcurrentHashMap<>();
    /**
     * domain --> config
     */
    private Map<String, ProxyConfig> domainToConfigIndex = new ConcurrentHashMap<>();
    /**
     * port --> config
     */
    private Map<Integer, ProxyConfig> portToConfigIndex = new ConcurrentHashMap<>();

    @Override
    public ProxyConfig add(ProxyConfig config) {
        return null;
    }

    @Override
    public ProxyConfig update(ProxyConfig config) {
        return null;
    }

    @Override
    public ProxyConfig findById(String proxyId) {
        return null;
    }

    @Override
    public List<ProxyConfig> findByClientId(String clientId) {
        return List.of();
    }

    @Override
    public ProxyConfig findByDomain(String domain) {
        return null;
    }

    @Override
    public ProxyConfig findByRemotePort(Integer remotePort) {
        return null;
    }

    @Override
    public List<ProxyConfig> findAll() {
        return List.of();
    }

    @Override
    public List<ProxyConfig> findAllHttpProxies() {
        return List.of();
    }

    @Override
    public List<ProxyConfig> findAllTcpProxies() {
        return List.of();
    }

    @Override
    public void deleteById(String proxyId) {

    }

    @Override
    public boolean existsById(String proxyId) {
        return false;
    }

    @Override
    public ProxyConfig findByClientIdAndName(String clientId, String proxyName) {
        return null;
    }
}
