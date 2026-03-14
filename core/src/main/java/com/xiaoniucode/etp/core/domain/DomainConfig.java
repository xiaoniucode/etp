package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
@Setter
@ToString
public class DomainConfig {
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
    private Boolean autoDomain;

    public void setFullDomains(Set<String>domains){
        this.fullDomains.clear();
        this.fullDomains.addAll(domains);
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
     * 计算域名的类型
     * 优先级：自定义域名 --> 子域名 --> 自动生成
     *
     * @return 域名类型
     */
    public DomainType getDomainType() {
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
}
