package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.enums.DomainType;
import lombok.Getter;

@Getter
public class AutoDomainInfo extends BaseDomainInfo {
    private final String baseDomain;
    private final String prefix;
    
    public AutoDomainInfo(String baseDomain, String prefix) {
        super(buildFullDomain(baseDomain, prefix), DomainType.AUTO);
        this.baseDomain = baseDomain;
        this.prefix = prefix;
    }
    
    private static String buildFullDomain(String base, String prefix) {
        return prefix.toLowerCase() + "." + base.toLowerCase();
    }
}