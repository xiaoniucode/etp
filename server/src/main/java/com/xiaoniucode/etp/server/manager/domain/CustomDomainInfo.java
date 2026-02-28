package com.xiaoniucode.etp.server.manager.domain;

import com.xiaoniucode.etp.core.enums.DomainType;

public class CustomDomainInfo extends BaseDomainInfo {
    public CustomDomainInfo(String customDomain) {
        super(normalize(customDomain), DomainType.CUSTOM_DOMAIN);
    }
    private static String normalize(String domain) {
        return domain.trim().toLowerCase();
    }
}
