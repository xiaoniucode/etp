package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.ProtocolType;

import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
public class ProxyConfig {
    /**
     * 唯一标识
     */
    @Setter
    private String proxyId;
    /**
     * 客户端内唯一
     */
    @Setter
    private String name;
    @Setter
    private ProtocolType protocol;
    @Setter
    private String localIp = "127.0.0.1";
    @Setter
    private Integer localPort;
    @Setter
    private Integer remotePort;
    /**
     * 代理的状态
     */
    @Setter
    private ProxyStatus status = ProxyStatus.OPEN;
    /**
     * 任意自定义域名
     */
    private final Set<String> customDomains = new CopyOnWriteArraySet<>();
    /**
     * 根据基础域名生成子域名
     */
    private final Set<String> subDomains = new CopyOnWriteArraySet<>();
    /**
     * 最终完整域名列表
     */
    private final Set<String> fullDomains = new CopyOnWriteArraySet<>();
    /**
     * 是否自动生成域名
     */
    @Setter
    private Boolean autoDomain = true;
    /**
     * 是否加密
     */
    @Setter
    private Boolean encrypt = false;
    /**
     * 是否压缩
     */
    @Setter
    private Boolean compress = false;
    /**
     * 访问控制
     */
    @Setter
    private AccessControlConfig accessControl;
    /**
     * HTTP 协议有效
     */
    @Setter
    private BasicAuthConfig basicAuth;
    /**
     * 带宽限制
     */
    @Setter
    private BandwidthConfig bandwidth;

    /**
     * 计算域名的类型
     * 优先级：自定义域名 --> 子域名 --> 自动生成
     *
     * @return 域名类型
     */
    public DomainType getDomainType() {
        if (ProtocolType.isTcp(protocol)) {
            return null;
        }
        if (!customDomains.isEmpty()) {
            return DomainType.CUSTOM_DOMAIN;
        }
        if (!this.subDomains.isEmpty()) {
            return DomainType.SUBDOMAIN;
        }
        if (getAutoDomain() != null && getAutoDomain()) {
            return DomainType.AUTO;
        }
        return null;
    }

    public boolean isOpen() {
        return this.status == ProxyStatus.OPEN;
    }

    public boolean hasStatus() {
        return status != null;
    }

    /**
     * 是否有自定义域名
     */
    public boolean hasCustomDomains() {
        return !customDomains.isEmpty();
    }

    /**
     * 是否有子域名
     */
    public boolean hasSubDomains() {
        return !subDomains.isEmpty();
    }

    /**
     * 是否有完整域名
     */
    public boolean hasFullDomains() {
        return !fullDomains.isEmpty();
    }

    /**
     * 是否启用了自动生成域名
     */
    public boolean isAutoDomainEnabled() {
        return autoDomain != null && autoDomain;
    }

    /**
     * 是否启用加密
     */
    public boolean isEncryptEnabled() {
        return encrypt != null && encrypt;
    }

    /**
     * 是否启用压缩
     */
    public boolean isCompressEnabled() {
        return compress != null && compress;
    }

    public boolean hasAccessControl() {
        return accessControl != null;
    }

    public boolean hasBandwidthLimit() {
        return bandwidth != null;
    }

    public boolean hasBasicAuth() {
        return basicAuth != null;
    }

    /**
     * 是否是 HTTP 协议
     */
    public boolean isHttp() {
        return ProtocolType.isHttp(protocol);
    }

    /**
     * 是否是 TCP 协议
     */
    public boolean isTcp() {
        return ProtocolType.isTcp(protocol);
    }

    public boolean hasRemotePort() {
        return remotePort != null;
    }
}
