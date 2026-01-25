package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.config.domain.ProxyConfig;

public class DomainInfo {
    private final String domain;
    private final ProxyConfig proxyConfig;
    private boolean active;

    public DomainInfo(String domain, ProxyConfig proxyConfig) {
        this.domain = domain;
        this.proxyConfig = proxyConfig;
        this.active = true;
    }

    public String getDomain() {
        return domain;
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}