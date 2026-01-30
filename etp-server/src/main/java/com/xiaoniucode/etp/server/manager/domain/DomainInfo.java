package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.enums.ProxyStatus;

public class DomainInfo {
    private final String domain;
    private final ProxyConfig proxyConfig;
    private boolean active;

    public DomainInfo(String domain, ProxyConfig proxyConfig) {
        this.domain = domain;
        this.proxyConfig = proxyConfig;
        this.active = proxyConfig.getStatus() == ProxyStatus.OPEN;
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