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

package com.xiaoniucode.etp.server.service;

import com.xiaoniucode.etp.core.enums.AgentType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
@Component
public class EmbeddedAgentRegistry {
    private static final AgentType AGENT_TYPE = AgentType.EMBEDDED;

    private final ConcurrentMap<String, AgentMeta> agentMetaMap = new ConcurrentHashMap<>();

    private AgentMeta getOrCreate(String agentId) {
        if (agentId == null) throw new IllegalArgumentException("agentId不能为空");
        return agentMetaMap.computeIfAbsent(agentId, AgentMeta::new);
    }

    public void addAgent(String agentId) {
        getOrCreate(agentId);
    }

    public void addProxyId(String agentId, String proxyId) {
        if (proxyId != null) getOrCreate(agentId).getProxyIds().add(proxyId);
    }

    public void addListenPort(String agentId, Integer listenPort) {
        if (listenPort != null) getOrCreate(agentId).getListenPorts().add(listenPort);
    }

    public void addDomain(String agentId, String domain) {
        if (domain != null) getOrCreate(agentId).getDomains().add(domain);
    }

    public void addDomains(String agentId, Collection<String> domains) {
        if (domains != null && !domains.isEmpty()) getOrCreate(agentId).getDomains().addAll(domains);
    }

    public AgentType identifyByAgentId(String agentId) {
        return agentMetaMap.containsKey(agentId) ? AGENT_TYPE : AgentType.UNKNOWN;
    }

    public AgentType identifyByProxyId(String proxyId) {
        if (proxyId == null) return AgentType.UNKNOWN;
        for (AgentMeta meta : agentMetaMap.values()) {
            if (meta.getProxyIds().contains(proxyId)) return AGENT_TYPE;
        }
        return AgentType.UNKNOWN;
    }

    public AgentType identifyByListenPort(Integer listenPort) {
        if (listenPort == null) return AgentType.UNKNOWN;
        for (AgentMeta meta : agentMetaMap.values()) {
            if (meta.getListenPorts().contains(listenPort)) return AGENT_TYPE;
        }
        return AgentType.UNKNOWN;
    }

    public AgentType identifyByDomain(String domain) {
        if (domain == null) return AgentType.UNKNOWN;
        for (AgentMeta meta : agentMetaMap.values()) {
            if (meta.getDomains().contains(domain)) return AGENT_TYPE;
        }
        return AgentType.UNKNOWN;
    }

    public void removeProxyId(String agentId, String proxyId) {
        AgentMeta meta = agentMetaMap.get(agentId);
        if (meta != null && proxyId != null) meta.getProxyIds().remove(proxyId);
    }

    public void removeListenPort(String agentId, Integer listenPort) {
        AgentMeta meta = agentMetaMap.get(agentId);
        if (meta != null && listenPort != null) meta.getListenPorts().remove(listenPort);
    }

    public void removeDomain(String agentId, String domain) {
        AgentMeta meta = agentMetaMap.get(agentId);
        if (meta != null && domain != null) meta.getDomains().remove(domain);
    }

    public void removeDomains(String agentId, Collection<String> domains) {
        AgentMeta meta = agentMetaMap.get(agentId);
        if (meta != null && domains != null) meta.getDomains().removeAll(domains);
    }

    public void removeAgent(String agentId) {
        if (agentId != null) agentMetaMap.remove(agentId);
    }
}