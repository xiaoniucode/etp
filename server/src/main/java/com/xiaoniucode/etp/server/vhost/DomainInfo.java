package com.xiaoniucode.etp.server.vhost;

import com.xiaoniucode.etp.core.enums.DomainType;

public interface DomainInfo {
    public String getFullDomain();
    public DomainType getDomainType();
    boolean isAvailable();
}