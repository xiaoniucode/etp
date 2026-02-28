package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.enums.DomainType;

public interface DomainInfo {
    public String getFullDomain();
    public DomainType getDomainType();
    boolean isAvailable();
}