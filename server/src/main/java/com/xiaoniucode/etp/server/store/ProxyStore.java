package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.core.domain.ProxyConfig;

import java.util.List;

public interface ProxyStore {
    ProxyConfig add(ProxyConfig config);

    ProxyConfig update(ProxyConfig config);

    ProxyConfig findById(String proxyId);

    List<ProxyConfig> findByClientId(String clientId);

    ProxyConfig findByDomain(String domain);

    ProxyConfig findByRemotePort(Integer remotePort);

    List<ProxyConfig> findAll();

    List<ProxyConfig> findAllHttpProxies();

    List<ProxyConfig> findAllTcpProxies();

    void deleteById(String proxyId);

    boolean existsById(String proxyId);

    ProxyConfig findByClientIdAndName(String clientId, String proxyName);
}
