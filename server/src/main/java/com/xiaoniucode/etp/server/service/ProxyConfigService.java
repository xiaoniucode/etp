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
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.service.repository.ProxyQueryRepository;
import com.xiaoniucode.etp.server.utils.DomainUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ProxyConfigService {
    @Autowired
    private ProxyQueryRepository proxyQueryRepository;
    @Resource
    private AppConfig appConfig;

    public Optional<ProxyConfig> findById(String proxyId) {
        return proxyQueryRepository.findById(proxyId);
    }

    public Optional<ProxyConfig> findByDomain(String domain) {
        String baseDomain = appConfig.getBaseDomain();

        if (!StringUtils.hasText(baseDomain)) {
            return proxyQueryRepository.findByDomain(domain);
        }
        String prefix = DomainUtils.extractPrefix(domain, baseDomain);

        if (prefix == null) {
            return proxyQueryRepository.findByDomain(domain);
        }
        return proxyQueryRepository.findBySubdomain(baseDomain, prefix);
    }

    public Optional<ProxyConfig> findByAgentAndName(String agentId, String proxyName) {
        return proxyQueryRepository.findByAgentAndName(agentId, proxyName);
    }

    public Optional<ProxyConfig> findByRemotePort(int remotePort) {
        return proxyQueryRepository.findByRemotePort(remotePort);
    }

    public List<Integer> getAllPorts() {
        return proxyQueryRepository.findAllListenPorts();
    }

    public List<Integer> findListenPortByAgentIdAndProxyStatus(String agentId, ProxyStatus proxyStatus) {
        return proxyQueryRepository.findListenPortByAgentIdAndProxyStatus(agentId,proxyStatus);
    }
}
