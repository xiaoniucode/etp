package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;

import java.util.List;
import java.util.Optional;

public interface ProxyManager {
    public ProxyConfig register(ProxyConfig proxyConfig) throws EtpException;

    public Optional<ProxyConfig> delete(String proxyId) throws EtpException;

    public Optional<ProxyConfig> findByRemotePort(Integer port);

    public List<ProxyConfig> findAll();

    /**
     * 查询所有 TCP 代理
     */
    List<ProxyConfig> findAllTcpProxies();

    /**
     * 查询所有 HTTP 代理
     */
    List<ProxyConfig> findAllHttpProxies();

    public List<ProxyConfig> findByClientId(String clientId);

    public Optional<ProxyConfig> findById(String proxyId);

    public Optional<ProxyConfig> findByDomain(String domain);

    Optional<ProxyConfig> findByClientIdAndName(String clientId, String proxyName);

    public ProxyConfig changeStatus(String proxyId, boolean enable) throws Exception;
}
