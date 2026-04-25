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
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.service.repository.ProxyQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProxyConfigService {
    @Autowired
    private ProxyQueryRepository proxyQueryRepository;
    @Autowired
    private ProxyManager proxyManager;

    public ProxyConfig findById(String proxyId) {
        Optional<ProxyConfig> byId = proxyQueryRepository.findById(proxyId);
        return null;
    }

    public List<ProxyConfig> findByAgentId(String agentId) {
        return proxyQueryRepository.findByAgentId(agentId);
    }

    public Optional<ProxyConfig> findByDomain(String domain) {
        return proxyQueryRepository.findByDomain(domain);
    }

    public ProxyConfig findByAgentAndName(String agentId, String proxyName) {
        return proxyQueryRepository.findByAgentAndName(agentId, proxyName).orElseGet(null);
    }

    public Optional<ProxyConfig> findByRemotePort(int remotePort) {
        return proxyQueryRepository.findByRemotePort(remotePort);
    }

    public List<Integer> getAgentAllPorts(String agentId) {
        return proxyQueryRepository.findAgentPortsByAgentId(agentId);
    }

    public List<Integer> getAllPorts() {
        return proxyQueryRepository.findAllPorts();
    }
}
