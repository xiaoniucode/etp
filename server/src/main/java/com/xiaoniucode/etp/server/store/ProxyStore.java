package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.core.domain.ProxyConfig;

import java.util.List;

public interface ProxyStore {
    ProxyConfig save(ProxyConfig config);

    void replace(ProxyConfig newProxyConfig);

    ProxyConfig findById(String proxyId);

    List<String> findProxyIdsByAgentId(String agentId);

    ProxyConfig findByRemotePort(Integer remotePort);

    void deleteById(String proxyId);

    void deleteByAgentId(String agentId);

    boolean existsById(String proxyId);

    ProxyConfig findByAgentIdAndName(String agentId, String proxyName);
}
