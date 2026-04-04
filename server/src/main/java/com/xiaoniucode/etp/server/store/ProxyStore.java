package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.core.domain.ProxyConfig;

import java.util.List;

public interface ProxyStore {
    ProxyConfig save(ProxyConfig config);

    boolean replace(ProxyConfig newProxyConfig);

    ProxyConfig findById(String proxyId);

    List<ProxyConfig> findByAgentId(String agentId);

    ProxyConfig findByRemotePort(Integer remotePort);

    List<ProxyConfig> findAll();

    List<ProxyConfig> findAllHttpProxies();

    List<ProxyConfig> findAllTcpProxies();

    void deleteById(String proxyId);

    void deleteByAgentId(String agentId);

    boolean existsById(String proxyId);

    ProxyConfig findByAgentIdAndName(String clientId, String proxyName);
}
