package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.codec.ProtocolType;

import java.util.Set;

public class ProxyMapping {
    private Integer proxyId;
    private String name;
    private ProtocolType type;
    private Integer localPort;
    private Integer remotePort;
    private Integer status;
    private Set<String> domains;
    public Integer getProxyId() {
        return proxyId;
    }

    public String getName() {
        return name;
    }

    public ProtocolType getType() {
        return type;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public Integer getStatus() {
        return status;
    }

    public Set<String> getDomains() {
        return domains;
    }

    public void setProxyId(Integer proxyId) {
        this.proxyId = proxyId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ProtocolType type) {
        this.type = type;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setDomains(Set<String> domains) {
        this.domains = domains;
    }
}
