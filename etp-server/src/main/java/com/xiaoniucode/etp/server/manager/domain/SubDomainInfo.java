package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.enums.DomainType;

public class SubDomainInfo extends BaseDomainInfo {
    private final String baseDomain;
    private final String subDomain;

    public SubDomainInfo(String baseDomain, String subDomain) {
        super(buildFullDomain(baseDomain, subDomain), DomainType.SUBDOMAIN);
        this.baseDomain = baseDomain;
        this.subDomain = subDomain;
    }

    private static String buildFullDomain(String base, String sub) {
        return sub.trim().toLowerCase() + "." + base.trim().toLowerCase();
    }

    public String getBaseDomain() { return baseDomain; }
    public String getSubDomain() { return subDomain; }
}
