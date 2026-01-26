package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.codec.ProtocolType;

import java.util.Set;

public class ProxyConfig {
    private Integer proxyId;
    private String name;
    private ProtocolType type;
    private String localIP;
    private Integer localPort;
    private Integer remotePort;
    private Integer status;
    private Set<String> customDomains;
    private Set<String> subDomains;
    private Boolean autoDomain;

    public Integer getProxyId() {
        return proxyId;
    }

    public String getName() {
        return name;
    }

    public ProtocolType getType() {
        return type;
    }

    public String getLocalIP() {
        return localIP;
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

    public Set<String> getCustomDomains() {
        return customDomains;
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

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
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

    public void setCustomDomains(Set<String> customDomains) {
        this.customDomains = customDomains;
    }

    public Set<String> getSubDomains() {
        return subDomains;
    }

    public void setSubDomains(Set<String> subDomains) {
        this.subDomains = subDomains;
    }

    public Boolean getAutoDomain() {
        return autoDomain;
    }

    public void setAutoDomain(Boolean autoDomain) {
        this.autoDomain = autoDomain;
    }

    /**
     * 计算域名的类型
     *
     * @return 域名类型
     */
    public DomainType getDomainType() {
        if (this.customDomains != null && !this.customDomains.isEmpty()) {
            return DomainType.CUSTOM_DOMAIN;
        }
        if (this.subDomains != null && !this.subDomains.isEmpty()) {
            return DomainType.SUBDOMAIN;
        }
        if (getAutoDomain() != null && getAutoDomain()) {
            return DomainType.AUTO;
        }
        return null;
    }
}
