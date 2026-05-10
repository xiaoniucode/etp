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

package com.xiaoniucode.etp.server.web.core.repository;

import com.xiaoniucode.etp.common.message.PageResult;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.service.ProxyConfigExt;
import com.xiaoniucode.etp.server.service.repository.ProxyStore;
import com.xiaoniucode.etp.server.service.repository.ProxyQueryRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository("embeddedProxyQueryRepository")
public class EmbeddedProxyQueryRepository implements ProxyQueryRepository, ProxyStore {
    private final Map<String/*proxyId*/, ProxyConfig> proxyMap = new ConcurrentHashMap<>();
    private final Map<String/*agentId*/, Set<String/*proxyId*/>> agentIdIndex = new ConcurrentHashMap<>();
    private final Map<Integer/*remotePort*/, String/*proxyId*/> listenPortIndex = new ConcurrentHashMap<>();
    private final Map<String/*domain*/, String/*proxyId*/> fullDomainIndex = new ConcurrentHashMap<>();
    private final Map<String/*proxyId*/, Set<String/*domain*/>> proxyDomainIndex = new ConcurrentHashMap<>();
    private final Map<String/*agentId*/, Map<String/*proxyName*/, String/*proxyId*/>> agentNameIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<ProxyConfig> findById(String proxyId) {
        return Optional.ofNullable(proxyMap.get(proxyId));
    }

    @Override
    public List<Integer> findAllListenPorts() {
        return new ArrayList<>(listenPortIndex.keySet());
    }

    @Override
    public Optional<ProxyConfig> findByAgentAndName(String agentId, String proxyName) {
        Map<String, String> nameMap = agentNameIndex.get(agentId);
        if (nameMap == null) return Optional.empty();

        String proxyId = nameMap.get(proxyName);
        return proxyId == null ? Optional.empty() : Optional.ofNullable(proxyMap.get(proxyId));
    }

    @Override
    public Optional<ProxyConfig> findByListenPort(int listenPort) {
        String proxyId = listenPortIndex.get(listenPort);
        return Optional.ofNullable(proxyId == null ? null : proxyMap.get(proxyId));
    }

    @Override
    public Optional<ProxyConfig> findByFullDomain(String domain) {
        String proxyId = fullDomainIndex.get(domain);
        return Optional.ofNullable(proxyId == null ? null : proxyMap.get(proxyId));
    }

    @Override
    public List<ProxyConfigExt> findByAgentId(String agentId) {
        Set<String> proxyIds = agentIdIndex.getOrDefault(agentId, Set.of());
        return proxyIds.stream()
                .map(proxyMap::get)
                .map(config -> {
                    Set<String> domains = findDomainsByProxyId(config.getProxyId());
                    return new ProxyConfigExt(config, domains);
                })
                .toList();
    }

    @Override
    public boolean existsByFullDomain(String fullDomain) {
        return fullDomainIndex.containsKey(fullDomain);
    }

    @Override
    public Set<String> findDomainsByProxyId(String proxyId) {
        return proxyDomainIndex.getOrDefault(proxyId, Set.of());
    }

    @Override
    public List<ProxyConfig> findAll() {
        return new ArrayList<>(proxyMap.values());
    }

    @Override
    public PageResult<ProxyConfig> findByPage(Integer page, Integer size) {
        List<ProxyConfig> all = new ArrayList<>(proxyMap.values());
        int total = all.size();
        int start = (page - 1) * size;
        if (start >= total) {
            return new PageResult<>(List.of(), total, page, size);
        }
        int end = Math.min(start + size, total);
        List<ProxyConfig> pageList = all.subList(start, end);
        return new PageResult<>(pageList, total, page, size);
    }

    @Override
    public void saveTcp(ProxyConfig proxyConfig) {
        String proxyId = proxyConfig.getProxyId();
        proxyMap.put(proxyId, proxyConfig);
        agentIdIndex.computeIfAbsent(proxyConfig.getAgentId(), k -> ConcurrentHashMap.newKeySet())
                .add(proxyId);
        listenPortIndex.put(proxyConfig.getListenPort(), proxyId);
        agentNameIndex
                .computeIfAbsent(proxyConfig.getAgentId(), k -> new ConcurrentHashMap<>())
                .put(proxyConfig.getName(), proxyId);
    }

    @Override
    public void saveHttp(ProxyConfig proxyConfig, Set<String> domains) {
        String proxyId = proxyConfig.getProxyId();
        proxyMap.put(proxyId, proxyConfig);

        agentIdIndex.computeIfAbsent(proxyConfig.getAgentId(), k -> ConcurrentHashMap.newKeySet())
                .add(proxyId);

        if (domains != null) {
            for (String domain : domains) {
                fullDomainIndex.put(domain, proxyId);
            }
            proxyDomainIndex.put(proxyId, new HashSet<>(domains));
        }

        agentNameIndex
                .computeIfAbsent(proxyConfig.getAgentId(), k -> new ConcurrentHashMap<>())
                .put(proxyConfig.getName(), proxyId);
    }

    @Override
    public void delete(String proxyId) {
        ProxyConfig removed = proxyMap.remove(proxyId);
        if (removed == null) return;

        Set<String> agentSet = agentIdIndex.get(removed.getAgentId());
        if (agentSet != null) agentSet.remove(proxyId);
        if (removed.isTcp()) {
            listenPortIndex.remove(removed.getRemotePort());
        }
        if (removed.isHttp()) {
            Set<String> domains = proxyDomainIndex.get(proxyId);
            if (domains != null) {
                for (String domain : domains) {
                    fullDomainIndex.remove(domain);
                }
                proxyDomainIndex.remove(proxyId);
            }
        }
        Map<String, String> nameMap = agentNameIndex.get(removed.getAgentId());
        if (nameMap != null) nameMap.remove(removed.getName());
    }

    @Override
    public void deleteByAgent(String agentId) {
        Set<String> proxyIds = agentIdIndex.getOrDefault(agentId, Set.of());
        for (String proxyId : new HashSet<>(proxyIds)) {
            delete(proxyId);
        }
    }
}
