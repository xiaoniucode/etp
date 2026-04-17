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

package com.xiaoniucode.etp.server.web.support.store;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.store.ProxyStore;
import com.xiaoniucode.etp.server.web.entity.ProxyDO;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class CompositeProxyStore implements ProxyStore {
    private final Logger logger = LoggerFactory.getLogger(CompositeProxyStore.class);
    @Autowired
    private MultiLevelCache multiLevelCache;
    @Autowired
    private ProxyRepository proxyRepository;
    private final String CACHE_NAME = "proxy";

    @Override
    public ProxyConfig save(ProxyConfig config) {
        logger.debug("multi save");
        return null;
    }

    @Override
    public boolean replace(ProxyConfig newProxyConfig) {
        return false;
    }

    @Override
    public ProxyConfig findById(String proxyId) {
        multiLevelCache.get(CACHE_NAME, "id:" + proxyId, () -> {
            ProxyDO proxyDO = proxyRepository.findById(proxyId).orElse(null);
            return null;
        });
        return null;
    }

    @Override
    public List<ProxyConfig> findByAgentId(String agentId) {
        return List.of();
    }

    @Override
    public ProxyConfig findByRemotePort(Integer remotePort) {
        return null;
    }

    @Override
    public List<ProxyConfig> findAll() {
        return List.of();
    }

    @Override
    public List<ProxyConfig> findAllHttpProxies() {
        return List.of();
    }

    @Override
    public List<ProxyConfig> findAllTcpProxies() {
        return List.of();
    }

    @Override
    public void deleteById(String proxyId) {

    }

    @Override
    public void deleteByAgentId(String agentId) {

    }

    @Override
    public boolean existsById(String proxyId) {
        return false;
    }

    @Override
    public ProxyConfig findByAgentIdAndName(String agentId, String proxyName) {
        return null;
    }
}
