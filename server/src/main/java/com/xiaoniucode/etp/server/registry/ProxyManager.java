package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;

import java.util.List;
import java.util.Optional;

public interface ProxyManager {
    public RegisterResult register(ProxyConfig proxyConfig) throws EtpException;

    public Optional<ProxyConfig> remove(String proxyId) throws EtpException;

    void clearByAgentId(String agentId);

    public Optional<ProxyConfig> findByRemotePort(Integer port);

    public List<ProxyConfig> findAll();

    List<ProxyConfig> findAllTcpProxies();

    List<ProxyConfig> findAllHttpProxies();

    public List<ProxyConfig> findByAgentId(String agentId);

    public Optional<ProxyConfig> findById(String proxyId);

    public Optional<ProxyConfig> findByDomain(String domain);

    Optional<ProxyConfig> findByAgentIdAndName(String agentId, String proxyName);

    public ProxyConfig changeStatus(String proxyId, boolean enable) throws EtpException;

    void batchRemove(List<String> ids);
}
