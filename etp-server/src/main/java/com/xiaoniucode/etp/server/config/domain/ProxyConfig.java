package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.enums.DomainType;
import com.xiaoniucode.etp.server.enums.ProxyStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class ProxyConfig {
    private String name;
    private ProtocolType protocol;
    private String localIp;
    private Integer localPort;
    private Integer remotePort;
    private ProxyStatus status;
    private Set<String> customDomains;
    private Set<String> subDomains;
    private Boolean autoDomain;

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
