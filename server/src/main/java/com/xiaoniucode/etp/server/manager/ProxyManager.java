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
import com.xiaoniucode.etp.server.port.PortAcceptor;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.security.IpAccessChecker;
import com.xiaoniucode.etp.server.service.DomainConfigService;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.vhost.DomainRegistry;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 运行时代理管理器，负责代理的注册、注销和状态变更等操作。
 */
@Component
public class ProxyManager {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyManager.class);
    private final Map<String/*proxyId*/, Integer/*listenPort*/> portMap = new ConcurrentHashMap<>();
    private final Map<String/*agentId*/, Set<String/*proxyId*/>> agentPortMap = new ConcurrentHashMap<>();
    private final Map<String/*proxyId*/, String/*agentId*/> proxyAgentMap = new ConcurrentHashMap<>();
    @Autowired
    private MetricsCollector metricsCollector;
    @Autowired
    private IpAccessChecker ipAccessChecker;
    @Autowired
    private PortAcceptor portAcceptor;
    @Autowired
    private PortManager portManager;
    @Autowired
    private DomainRegistry domainRegistry;
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private DomainConfigService domainConfigService;

    /**
     * 激活代理
     *
     * @param config 代理配置信息
     * @throws EtpException 异常
     */
    public void activate(ProxyConfig config) throws EtpException {
        if (config == null || config.getStatus().isClosed()) {
            return;
        }
        logger.debug("激活代理: {}", config.getProxyId());
        String agentId = config.getAgentId();
        String proxyId = config.getProxyId();
        proxyAgentMap.put(proxyId, agentId);

        if (config.isTcp()) {
            Integer listenPort = config.getListenPort();
            portMap.put(proxyId, listenPort);
            portAcceptor.bindPort(listenPort);
            agentPortMap.computeIfAbsent(config.getAgentId(), k -> ConcurrentHashMap.newKeySet())
                    .add(proxyId);
        }
        if (config.isHttp()) {
            if (domainRegistry.exists(proxyId)) {
                return;
            }
            Set<String> domains = domainConfigService.findDomainsByProxyId(proxyId);
            if (!CollectionUtils.isEmpty(domains)) {
                //将域名注册到域名注册中心
                domainRegistry.register(proxyId, domains);
            }
        }
    }

    /**
     * 停用代理
     *
     * @throws EtpException
     */
    public void deactivate(String proxyId) {
        logger.debug("停用代理: {}", proxyId);
        Integer listenPort = portMap.remove(proxyId);
        if (listenPort != null) {
            shutdownPortResources(listenPort);
        }
        String agentId = proxyAgentMap.remove(proxyId);
        if (agentId != null) {
            Set<String> set = agentPortMap.get(agentId);
            if (set != null) {
                set.remove(proxyId);
            }
        }
        Set<String> domains = domainRegistry.getDomainsByProxyId(proxyId);
        for (String domain : domains) {
            streamManager.fireCloseByDomain(domain);
        }
        domainRegistry.unregister(proxyId);//从注册中心删除
        //删除IP访问控制
        ipAccessChecker.invalidate(proxyId);
        //删除代理流量统计记录
        metricsCollector.removeByProxyId(proxyId);
    }

    public void deactivates(List<String> proxyIds) {
        if (CollectionUtils.isEmpty(proxyIds)) {
            return;
        }
        proxyIds.forEach(this::deactivate);
    }

    public void reconcile(ProxyConfig config) throws EtpException {
        logger.debug("更新代理：{}", config.getProxyId());
        String proxyId = config.getProxyId();
        if (config.getStatus().isClosed()) {
            deactivate(proxyId);
            return;
        }
        deactivate(proxyId);
        activate(config);
    }

    /**
     * 批量停用某个 Agent 下的代理
     *
     * @throws EtpException
     */
    public void onAgentOffline(String agentId) throws EtpException {
        logger.debug("停用客户端 {} 所有代理", agentId);
        Set<String> proxyIds = agentPortMap.get(agentId);
        if (!CollectionUtils.isEmpty(proxyIds)) {
            for (String proxyId : proxyIds) {
                deactivate(proxyId);
            }
        }
    }

    private void shutdownPortResources(int listenPort) {
        portManager.release(listenPort);
        portAcceptor.stopPortListen(listenPort);
        streamManager.fireCloseByPort(listenPort);
    }
}