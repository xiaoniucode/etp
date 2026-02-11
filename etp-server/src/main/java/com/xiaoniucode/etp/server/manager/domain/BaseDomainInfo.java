package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.enums.DomainType;

public abstract class BaseDomainInfo implements DomainInfo {
    protected final String fullDomain;
    protected final DomainType domainType;
    
    public BaseDomainInfo(String fullDomain, DomainType domainType) {
        this.fullDomain = fullDomain;
        this.domainType = domainType;
    }
    
    @Override
    public String getFullDomain() {
        return fullDomain;
    }
    
    @Override
    public DomainType getDomainType() {
        return domainType;
    }
    
    @Override
    public boolean isAvailable() {
        return fullDomain != null && !fullDomain.trim().isEmpty();
    }
}
