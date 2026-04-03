/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.vhost.DomainBinding;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存实现的域名存储，用于管理域名与代理的绑定关系
 */
@Component
public class InMemoryDomainStore implements DomainStore {

    /**
     * 域名到 DomainBinding 的映射，
     */
    private final Map<String, DomainBinding> domainMap = new ConcurrentHashMap<>();

    /**
     * 代理 ID 到域名集合的映射
     */
    private final Map<String, Set<String>> proxyToDomains = new ConcurrentHashMap<>();

    /**
     * 代理 ID 到 DomainBinding 列表的缓存
     */
    private final Map<String, List<DomainBinding>> proxyToBindingsCache = new ConcurrentHashMap<>();

    /**
     * 检查域名是否已被占用
     *
     * @param domain 域名
     * @return true 表示域名已被占用，false 表示未被占用
     */
    @Override
    public boolean isOccupied(String domain) {
        return domain != null && domainMap.containsKey(domain);
    }

    /**
     * 保存域名绑定关系
     *
     * @param binding 域名绑定对象
     */
    @Override
    public void save(DomainBinding binding) {
        if (binding == null || binding.getDomain() == null || binding.getProxyId() == null) {
            return;
        }

        String domain = binding.getDomain();
        String proxyId = binding.getProxyId();

        domainMap.put(domain, binding);

        Set<String> domains = proxyToDomains.computeIfAbsent(proxyId, k -> ConcurrentHashMap.newKeySet());
        domains.add(domain);

        // 重新构建 List
        rebuildProxyCache(proxyId);
    }

    /**
     * 根据域名查找绑定关系
     *
     * @param domain 域名
     * @return 域名绑定对象的 Optional，若不存在则返回空 Optional
     */
    @Override
    public Optional<DomainBinding> findByDomain(String domain) {
        if (domain == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(domainMap.get(domain));
    }

    /**
     * 根据代理 ID 查找所有绑定关系
     *
     * @param proxyId 代理 ID
     * @return 绑定关系列表，若不存在则返回空列表
     */
    @Override
    public List<DomainBinding> findByProxyId(String proxyId) {
        if (proxyId == null) {
            return List.of();
        }
        List<DomainBinding> cached = proxyToBindingsCache.get(proxyId);
        return cached != null ? List.copyOf(cached) : List.of();
    }

    /**
     * 删除指定代理的所有绑定关系
     *
     * @param proxyId 代理 ID
     */
    @Override
    public void delete(String proxyId) {
        if (proxyId == null) {
            return;
        }

        Set<String> domains = proxyToDomains.remove(proxyId);
        if (domains == null || domains.isEmpty()) {
            proxyToBindingsCache.remove(proxyId);
            return;
        }

        for (String domain : domains) {
            domainMap.remove(domain);
        }

        proxyToBindingsCache.remove(proxyId);
    }

    /**
     * 删除指定代理的指定域名绑定关系
     *
     * @param proxyId 代理 ID
     * @param domain  域名
     */
    @Override
    public void delete(String proxyId, String domain) {
        if (proxyId == null || domain == null) {
            return;
        }

        domainMap.remove(domain);
        Set<String> domains = proxyToDomains.get(proxyId);
        if (domains != null) {
            domains.remove(domain);
            if (domains.isEmpty()) {
                proxyToDomains.remove(proxyId);
                proxyToBindingsCache.remove(proxyId);
            } else {
                rebuildProxyCache(proxyId);
            }
        }
    }

    /**
     * 重建指定代理的绑定缓存
     *
     * @param proxyId 代理 ID
     */
    private void rebuildProxyCache(String proxyId) {
        Set<String> domains = proxyToDomains.get(proxyId);
        if (domains == null || domains.isEmpty()) {
            proxyToBindingsCache.remove(proxyId);
            return;
        }

        List<DomainBinding> list = new ArrayList<>(domains.size());
        for (String d : domains) {
            DomainBinding b = domainMap.get(d);
            if (b != null) {
                list.add(b);
            }
        }
        proxyToBindingsCache.put(proxyId, list);
    }
}