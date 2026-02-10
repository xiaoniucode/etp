package com.xiaoniucode.etp.server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    private final Map<String, Set<String>> proxyToDomains = new ConcurrentHashMap<>();
    private final Map<String, String> domainToProxyId = new ConcurrentHashMap<>();

    /**
     * 添加域名
     *
     * @param proxyId 代理ID
     * @param domain  域名
     * @return 如果添加成功返回该域名
     */
    public String addDomain(String proxyId, String domain) {
        if (proxyId == null || domain == null) return null;

        String existingProxy = domainToProxyId.get(domain);
        if (existingProxy != null && !existingProxy.equals(proxyId)) {
            logger.warn("域名已被占用: {}", domain);
            return null;
        }
        domainToProxyId.put(domain, proxyId);
        proxyToDomains.computeIfAbsent(proxyId, k -> ConcurrentHashMap.newKeySet()).add(domain);
        return domain;
    }

    public Set<String> addDomains(String proxyId, Set<String> domains) {
        Set<String> d = new HashSet<>();
        if (proxyId == null || domains == null) return d;

        for (String domain : domains) {
            if (addDomain(proxyId, domain) != null) {
                d.add(domain);
            }
        }
        return d;
    }

    public void removeDomain(String proxyId, String domain) {
        if (proxyId == null || domain == null) return;

        String owner = domainToProxyId.get(domain);
        if (owner == null || !owner.equals(proxyId)) return;

        domainToProxyId.remove(domain);
        Set<String> domainsSet = proxyToDomains.get(proxyId);
        if (domainsSet != null) {
            domainsSet.remove(domain);
            if (domainsSet.isEmpty()) {
                proxyToDomains.remove(proxyId);
            }
        }
    }

    public void clearDomain(String proxyId) {
        if (proxyId == null) return;

        Set<String> domains = proxyToDomains.remove(proxyId);
        if (domains != null) {
            for (String domain : domains) {
                domainToProxyId.remove(domain);
            }
        }
    }

    public boolean exist(String domain) {
        return domain != null && domainToProxyId.containsKey(domain);
    }

    public Set<String> getDomains(String proxyId) {
        if (proxyId == null) return Collections.emptySet();
        Set<String> domains = proxyToDomains.get(proxyId);
        return domains != null ? Collections.unmodifiableSet(domains) : Collections.emptySet();
    }

    public String getProxyId(String domain) {
        return domain != null ? domainToProxyId.get(domain) : null;
    }
}
