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

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.server.service.repository.ProxyQueryRepository;
import com.xiaoniucode.etp.server.vhost.DomainInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProxyConfigService {
    @Autowired
    private ProxyQueryRepositoryRouter proxyQueryRepositoryRouter;
    @Autowired
    private EmbeddedAgentRegistry embeddedAgentRegistry;

    public Optional<ProxyConfig> findById(String proxyId) {
        AgentType type = embeddedAgentRegistry.identifyByProxyId(proxyId);
        ProxyQueryRepository repo = proxyQueryRepositoryRouter.route(type);
        return repo.findById(proxyId);
    }

    public Optional<ProxyConfig> findByDomain(String domain) {
        AgentType type = embeddedAgentRegistry.identifyByDomain(domain);
        ProxyQueryRepository repo = proxyQueryRepositoryRouter.route(type);
        return repo.findByFullDomain(domain);
    }

    public Optional<ProxyConfig> findByAgentAndName(String agentId, String proxyName) {
        AgentType type = embeddedAgentRegistry.identifyByAgentId(agentId);
        ProxyQueryRepository repo = proxyQueryRepositoryRouter.route(type);
        return repo.findByAgentAndName(agentId, proxyName);
    }

    public Optional<ProxyConfig> findByListenPort(int remotePort) {
        AgentType type = embeddedAgentRegistry.identifyByListenPort(remotePort);
        ProxyQueryRepository repo = proxyQueryRepositoryRouter.route(type);
        return repo.findByListenPort(remotePort);
    }

    public boolean exists(String fullDomain) {
        AgentType type = embeddedAgentRegistry.identifyByDomain(fullDomain);
        ProxyQueryRepository repo = proxyQueryRepositoryRouter.route(type);
        return repo.existsByFullDomain(fullDomain);
    }

    public Set<DomainInfo> findDomainsByProxyId(String proxyId) {
        AgentType type = embeddedAgentRegistry.identifyByProxyId(proxyId);
        ProxyQueryRepository repo = proxyQueryRepositoryRouter.route(type);
        return repo.findDomainsByProxyId(proxyId);
    }

    public List<Integer> getAllListenPorts() {
        return proxyQueryRepositoryRouter.route(AgentType.STANDALONE).findAllListenPorts();
    }

    public List<ProxyConfigExt> findByAgentId(String agentId) {
        AgentType type = embeddedAgentRegistry.identifyByAgentId(agentId);
        ProxyQueryRepository repo = proxyQueryRepositoryRouter.route(type);
        return repo.findByAgentId(agentId);
    }
}