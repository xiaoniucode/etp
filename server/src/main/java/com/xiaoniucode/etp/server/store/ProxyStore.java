package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.core.domain.ProxyConfig;

import java.util.List;

public interface ProxyStore {
    ProxyConfig add(ProxyConfig config);

    boolean replace(ProxyConfig newProxyConfig);

    ProxyConfig findById(String proxyId);

    List<ProxyConfig> findByClientId(String clientId);

    ProxyConfig findByRemotePort(Integer remotePort);

    List<ProxyConfig> findAll();

    List<ProxyConfig> findAllHttpProxies();

    List<ProxyConfig> findAllTcpProxies();

    void deleteById(String proxyId);

    void deleteByClientId(String clientId);

    boolean existsById(String proxyId);

    ProxyConfig findByClientIdAndName(String clientId, String proxyName);
}
