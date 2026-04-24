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

package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.security.IpAccessChecker;
import com.xiaoniucode.etp.server.service.handler.ConfigHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 运行时代理管理器，负责代理的注册、注销和状态变更等操作。
 */
@Component
public class ProxyManager {
    private Map<String/*proxyId*/, ProxyConfig> proxyRegistry = new ConcurrentHashMap<>();
    private Map<String/*agentId*/, List<String/*proxyId*/>> agentProxyIndex = new ConcurrentHashMap<>();
    private Map<Integer/*listenPort*/, String/*proxyId*/> portProxyIndex = new ConcurrentHashMap<>();
    private Map<String/*domain*/, String/*proxyId*/> domainProxyIndex = new ConcurrentHashMap<>();
    @Autowired
    private ConfigHandlerFactory configHandlerFactory;
    @Autowired
    private MetricsCollector metricsCollector;
    @Autowired
    private IpAccessChecker ipAccessChecker;

    public void register(ProxyConfig proxyConfig) throws EtpException {
        String proxyId = proxyConfig.getProxyId();
        String agentId = proxyConfig.getAgentId();
        proxyRegistry.put(proxyId, proxyConfig);
        configHandlerFactory.getHandler(proxyConfig).handRegister(proxyConfig);
    }

    public void unregister(String proxyId) throws EtpException {

    }

    public void unregisterAll(String agentId) throws EtpException {

    }

    public void changeStatus(String proxyId, boolean enabled) throws EtpException {

    }

    public boolean exist(String proxyId) {
        return false;
    }
}