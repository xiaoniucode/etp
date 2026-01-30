package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.server.enums.DomainType;
import com.xiaoniucode.etp.server.enums.ProxyStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class ProxyConfig {
    /**
     * 代理的名称，不能重复
     */
    private String name;
    private ProtocolType protocol;
    private String localIp;
    private Integer localPort;
    private Integer remotePort;
    /**
     * 代理的状态
     */
    private ProxyStatus status;
    /**
     * 任意自定义域名
     */
    private Set<String> customDomains;
    /**
     * 根据基础域名生成子域名
     */
    private Set<String> subDomains;
    /**
     * 是否自动生成域名
     */
    private Boolean autoDomain;
    /**
     * 是否加密
     */
    private Boolean encrypt;
    /**
     * 是否压缩
     */
    private Boolean compress;
    /**
     * 最终完整域名列表
     */
    private Set<String> fullDomains;

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
