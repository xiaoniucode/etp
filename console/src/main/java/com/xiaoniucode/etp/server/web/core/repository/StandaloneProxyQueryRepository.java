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

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.service.ProxyConfigExt;
import com.xiaoniucode.etp.server.service.repository.ProxyQueryRepository;
import com.xiaoniucode.etp.server.web.core.repository.assembler.ProxyConfigAssembler;
import com.xiaoniucode.etp.server.web.dto.proxy.ProxyDetailQueryResult;
import com.xiaoniucode.etp.server.web.entity.BasicUserDO;
import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import com.xiaoniucode.etp.server.web.entity.ProxyDomainDO;
import com.xiaoniucode.etp.server.web.entity.ProxyTargetDO;
import com.xiaoniucode.etp.server.web.repository.BasicUserRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Repository("standaloneProxyQueryRepository")
public class StandaloneProxyQueryRepository implements ProxyQueryRepository {
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ProxyConfigAssembler proxyConfigAssembler;
    @Autowired
    private ProxyTargetRepository proxyTargetRepository;
    @Autowired
    private ProxyDomainRepository proxyDomainRepository;
    @Autowired
    private BasicUserRepository basicUserRepository;

    @Override
    public Optional<ProxyConfig> findById(String proxyId) {
        ProxyDetailQueryResult result = proxyRepository.findDetailByProxyId(proxyId);
        return Optional.ofNullable(assembleProxyConfig(result));
    }

    private ProxyConfig assembleProxyConfig(ProxyDetailQueryResult result) {
        if (result == null) {
            return null;
        }
        ProxyConfig config = proxyConfigAssembler.assembleBase(result);
        if (config == null) {
            return null;
        }

        List<ProxyTargetDO> targets = proxyTargetRepository.findByProxyId(config.getProxyId());
        proxyConfigAssembler.assembleTargets(config, targets);
        if (config.getProtocol().isHttp()) {
            List<ProxyDomainDO> domainDOs = proxyDomainRepository.findByProxyId(config.getProxyId());
            proxyConfigAssembler.assembleDomains(config, domainDOs);
            if (result.getBasicAuthDO() != null) {
                String proxyId = result.getBasicAuthDO().getProxyId();
                List<BasicUserDO> basicUsers = basicUserRepository.findByProxyId(proxyId);
                proxyConfigAssembler.assembleBasicAuthUsers(config, basicUsers);
            }
        }
        return config;
    }

    @Override
    public List<Integer> findAllListenPorts() {
        return proxyRepository.findAllListenPorts();
    }

    @Override
    public Optional<ProxyConfig> findByAgentAndName(String agentId, String proxyName) {
        ProxyDetailQueryResult result = proxyRepository.findDetailByAgentIdAndProxyName(agentId, proxyName);
        return Optional.ofNullable(assembleProxyConfig(result));
    }

    @Override
    public Optional<ProxyConfig> findByListenPort(int listenPort) {
        ProxyDetailQueryResult result = proxyRepository.findDetailByListenPort(listenPort);
        return Optional.ofNullable(assembleProxyConfig(result));
    }

    @Override
    public Optional<ProxyConfig> findByFullDomain(String domain) {
        Optional<ProxyDomainDO> domainDO = proxyDomainRepository.findByFullDomain(domain);
        if (domainDO.isEmpty()) {
            return Optional.empty();
        }
        String proxyId = domainDO.get().getProxyId();
        return findById(proxyId);
    }

    @Override
    public List<ProxyConfigExt> findByAgentId(String agentId) {
        List<ProxyDO> list = proxyRepository.findByAgentId(agentId);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        List<String> proxyIds = list.stream().map(ProxyDO::getId).toList();
        List<ProxyDomainDO> proxyDomainDOs = proxyDomainRepository.findByProxyIdIn(proxyIds);
        Map<String, List<ProxyDomainDO>> domainMap = proxyDomainDOs.stream()
                .collect(Collectors.groupingBy(ProxyDomainDO::getProxyId));
        return proxyConfigAssembler.assembleList(list).stream()
                .map(config -> {
                    List<ProxyDomainDO> domains = domainMap.getOrDefault(config.getProxyId(), List.of());
                    Set<String> domainList = domains.stream()
                            .map(ProxyDomainDO::getDomain)
                            .collect(Collectors.toSet());
                    return new ProxyConfigExt(config, domainList);
                })
                .toList();
    }

    @Override
    public boolean existsByFullDomain(String fullDomain) {
        return proxyDomainRepository.existsByFullDomain(fullDomain);
    }

    @Override
    public Set<String> findDomainsByProxyId(String proxyId) {
        return proxyDomainRepository.findFullDomainsByProxyId(proxyId);

    }
}
