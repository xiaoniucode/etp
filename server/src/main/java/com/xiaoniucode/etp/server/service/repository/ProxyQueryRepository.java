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

package com.xiaoniucode.etp.server.service.repository;

import com.xiaoniucode.etp.core.domain.ProxyConfig;

import java.util.List;
import java.util.Optional;

public interface ProxyQueryRepository {
    Optional<ProxyConfig> findById(String proxyId);

    List<Integer> findAgentPortsByAgentId(String agentId);

    List<Integer> findAllListenPorts();

    List<ProxyConfig> findByAgentId(String agentId);

    Optional<ProxyConfig> findByAgentAndName(String agentId, String proxyName);

    Optional<ProxyConfig> findByRemotePort(int remotePort);

    Optional<ProxyConfig> findByDomain(String domain);

    Optional<ProxyConfig> findBySubdomain(String baseDomain, String prefix);
}
