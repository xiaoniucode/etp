package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;

import java.util.List;
import java.util.Optional;

public interface ProxyManager {
    RegisterResult register(ProxyConfig proxyConfig) throws EtpException;

    Optional<ProxyConfig> remove(String proxyId) throws EtpException;

    void clearByAgentId(String agentId);

    Optional<ProxyConfig> findByRemotePort(Integer port);

    List<String> findProxyIdsByAgentId(String agentId);

    Optional<ProxyConfig> findById(String proxyId);

    Optional<ProxyConfig> findByDomain(String domain);

    Optional<ProxyConfig> findByAgentIdAndName(String agentId, String proxyName);

    ProxyConfig changeStatus(String proxyId, boolean enable) throws EtpException;

    void batchRemove(List<String> ids);
}
