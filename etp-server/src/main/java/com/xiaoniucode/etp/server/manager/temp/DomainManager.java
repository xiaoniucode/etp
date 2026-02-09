package com.xiaoniucode.etp.server.manager.temp;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 域名管理器
 */
@Component
public class DomainManager {
    private final Map<String, Set<String>> proxyToDomains = new ConcurrentHashMap<>();
    private final Map<String, String> domainToProxyId = new ConcurrentHashMap<>();

    public void addDomain(String proxyId, String domain) {
        if (proxyId == null || domain == null) return;

        String existingProxy = domainToProxyId.get(domain);
        if (existingProxy != null && !existingProxy.equals(proxyId)) {
            throw new IllegalStateException("域名已被占用: " + domain + " -> " + existingProxy);
        }

        domainToProxyId.put(domain, proxyId);
        proxyToDomains.computeIfAbsent(proxyId, k -> ConcurrentHashMap.newKeySet()).add(domain);
    }

    public void addDomains(String proxyId, Set<String> domains) {
        if (proxyId == null || domains == null) return;
        for (String domain : domains) {
            addDomain(proxyId, domain);
        }
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
