package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 域名管理器
 */
@Component
public class DomainManager {
    private final Logger logger = LoggerFactory.getLogger(DomainManager.class);
    /**
     * proxyId --> DomainInfo
     */
    private final Map<String, Set<DomainInfo>> proxyToDomains = new ConcurrentHashMap<>();
    /**
     * DomainInfo --> proxyId
     */
    private final Map<DomainInfo, String> domainInfoToProxyId = new ConcurrentHashMap<>();
    /**
     * Query
     * domain string --> ProxyId
     */
    private final Map<String, String> domainToProxyId = new ConcurrentHashMap<>();
    /**
     * Query
     * domain --> DomainInfo
     */
    private final Map<String, DomainInfo> domainToDomainInfo = new ConcurrentHashMap<>();

    /**
     * 添加域名
     *
     * @param proxyId    代理ID
     * @param domainInfo 域名
     * @return 如果添加成功返回该域名信息
     */
    public DomainInfo addDomain(String proxyId, DomainInfo domainInfo) {
        if (proxyId == null || domainInfo == null) return null;

        String existingProxy = domainInfoToProxyId.get(domainInfo);
        if (existingProxy != null && !existingProxy.equals(proxyId)) {
            logger.warn("域名已被占用: {}", domainInfo);
            return null;
        }
        domainInfoToProxyId.put(domainInfo, proxyId);
        domainToProxyId.put(domainInfo.getFullDomain(), proxyId);
        domainToDomainInfo.put(domainInfo.getFullDomain(), domainInfo);
        proxyToDomains.computeIfAbsent(proxyId, k -> ConcurrentHashMap.newKeySet()).add(domainInfo);
        return domainInfo;
    }

    public Set<DomainInfo> addDomains(String proxyId, Set<DomainInfo> domainInfos) {
        Set<DomainInfo> d = new HashSet<>();
        if (proxyId == null || domainInfos == null) return d;

        for (DomainInfo domain : domainInfos) {
            if (addDomain(proxyId, domain) != null) {
                d.add(domain);
            }
        }
        return d;
    }

    public void removeDomain(String proxyId, DomainInfo domainInfo) {
        if (proxyId == null || domainInfo == null) return;

        String owner = domainInfoToProxyId.get(domainInfo);
        if (owner == null || !owner.equals(proxyId)) return;

        String fullDomain = domainInfo.getFullDomain();
        domainToProxyId.remove(fullDomain);
        domainToDomainInfo.remove(fullDomain);

        domainInfoToProxyId.remove(domainInfo);
        Set<DomainInfo> domainsSet = proxyToDomains.get(proxyId);
        if (domainsSet != null) {
            domainsSet.remove(domainInfo);
            if (domainsSet.isEmpty()) {
                proxyToDomains.remove(proxyId);
            }
        }
    }

    public void clearDomain(String proxyId) {
        if (proxyId == null) return;

        Set<DomainInfo> domains = proxyToDomains.remove(proxyId);
        if (domains != null) {
            for (DomainInfo domainInfo : domains) {
                domainInfoToProxyId.remove(domainInfo);
                domainToProxyId.remove(domainInfo.getFullDomain());
                domainToDomainInfo.remove(domainInfo.getFullDomain());
            }
        }
    }

    public boolean exists(DomainInfo domainInfo) {
        return domainInfo != null && domainInfoToProxyId.containsKey(domainInfo);
    }

    public DomainInfo getDomainInfo(String domain) {
        if (!StringUtils.hasText(domain)) return null;
        return domainToDomainInfo.get(domain);
    }

    public boolean exists(String domain) {
        return domainToDomainInfo.containsKey(domain);
    }

    public Set<DomainInfo> getDomains(String proxyId) {
        if (proxyId == null) return Collections.emptySet();
        Set<DomainInfo> domains = proxyToDomains.get(proxyId);
        return domains != null ? Collections.unmodifiableSet(domains) : Collections.emptySet();
    }

    public String getProxyId(DomainInfo domainInfo) {
        return domainInfo != null ? domainInfoToProxyId.get(domainInfo) : null;
    }

    public String getProxyId(String domain) {
        if (domain == null) {
            return null;
        }
        return domainToProxyId.get(domain);
    }
}
