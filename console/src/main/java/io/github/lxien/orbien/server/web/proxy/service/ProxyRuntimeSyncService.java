/*
 *    Copyright 2026 lxien
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

package io.github.lxien.orbien.server.web.proxy.service;

import io.github.lxien.orbien.core.domain.HealthCheckConfig;
import io.github.lxien.orbien.core.domain.ProxyConfig;
import io.github.lxien.orbien.core.domain.ProxyConfigExt;
import io.github.lxien.orbien.core.message.Message;
import io.github.lxien.orbien.core.message.support.RuntimeInfoSupport;
import io.github.lxien.orbien.server.config.AppConfig;
import io.github.lxien.orbien.server.loadbalance.HealthManager;
import io.github.lxien.orbien.server.security.IpAccessChecker;
import io.github.lxien.orbien.server.security.TimeAccessChecker;
import io.github.lxien.orbien.server.service.ProxyConfigService;
import io.github.lxien.orbien.server.service.repository.ProxyQueryRepository;
import io.github.lxien.orbien.server.statemachine.stream.StreamManager;
import io.github.lxien.orbien.server.transport.http.BasicAuthHandler;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 将代理运行时配置推送给在线客户端
 */
@Service
public class ProxyRuntimeSyncService {
    private final Logger logger = LoggerFactory.getLogger(ProxyRuntimeSyncService.class);

    @Autowired
    private ProxyQueryRepository proxyQueryRepository;
    @Autowired
    private ProxyConfigSyncService proxyConfigSyncService;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private HealthManager healthManager;
    @Autowired
    private ProxyConfigService proxyConfigService;

    @Autowired
    private IpAccessChecker ipAccessChecker;
    @Autowired
    private TimeAccessChecker timeAccessChecker;
    @Autowired
    private BasicAuthHandler basicAuthHandler;
    @Autowired
    private StreamManager streamManager;

    public void refreshServerEntryPolicy(String proxyId) {
        if (proxyId == null) {
            return;
        }
        proxyConfigService.evictByProxyId(proxyId);
        ipAccessChecker.invalidate(proxyId);
        timeAccessChecker.invalidate(proxyId);
        basicAuthHandler.invalidate(proxyId);
    }

    public void refreshBasicAuthPolicy(String proxyId) {
        if (proxyId == null) {
            return;
        }
        proxyConfigService.evictByProxyId(proxyId);
        basicAuthHandler.invalidateAndDisconnect(proxyId);
    }

    public void refreshSocks5AuthPolicy(String proxyId) {
        if (proxyId == null) {
            return;
        }
        proxyConfigService.evictByProxyId(proxyId);
        ProxyConfigExt ext = proxyQueryRepository.findById(proxyId);
        if (ext == null || ext.getProxyConfig() == null || ext.getProxyConfig().getListenPort() == null) {
            return;
        }
        streamManager.fireCloseByPort(ext.getProxyConfig().getListenPort());
        logger.debug("SOCKS5 鉴权变更，已断开代理 {} 端口 {} 的现有会话",
                proxyId, ext.getProxyConfig().getListenPort());
    }

    public void syncProxyCreated(String proxyId) {
        publishRuntimeProxy(proxyId, true);
    }

    public void syncProxy(String proxyId) {
        publishRuntimeProxy(proxyId, false);
    }

    public void syncProxyRemoved(String agentId, String proxyId) {
        if (!StringUtils.hasText(proxyId)) {
            return;
        }
        proxyConfigService.evictByProxyId(proxyId);
        if (!StringUtils.hasText(agentId)) {
            return;
        }
        proxyConfigSyncService.syncOnDelete(agentId, proxyId);
    }

    public void syncProxiesRemoved(Map<String, List<String>> agentProxyIds) {
        if (CollectionUtils.isEmpty(agentProxyIds)) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : agentProxyIds.entrySet()) {
            String agentId = entry.getKey();
            List<String> proxyIds = entry.getValue();
            if (!StringUtils.hasText(agentId) || CollectionUtils.isEmpty(proxyIds)) {
                continue;
            }
            for (String proxyId : proxyIds) {
                proxyConfigService.evictByProxyId(proxyId);
            }
            if (proxyIds.size() == 1) {
                proxyConfigSyncService.syncOnDelete(agentId, proxyIds.getFirst());
            } else {
                proxyConfigSyncService.syncOnBatchDelete(agentId, proxyIds);
            }
        }
    }

    private void publishRuntimeProxy(String proxyId, boolean create) {
        proxyConfigService.evictByProxyId(proxyId);
        ProxyConfigExt ext = proxyQueryRepository.findById(proxyId);
        if (ext == null || ext.getProxyConfig() == null) {
            logger.debug("跳过运行时同步，代理不存在: {}", proxyId);
            return;
        }
        ProxyConfig config = ext.getProxyConfig();
        if (config.isSocks5()) {
            logger.debug("跳过 SOCKS5 运行配置同步: {}", proxyId);
            return;
        }
        if (!config.getStatus().isOpen()) {
            logger.debug("跳过运行时同步，代理未启用: {}", proxyId);
            return;
        }
        applyHealthCheckLifecycle(config);
        List<String> remoteAddrs = RuntimeInfoSupport.buildRemoteAddrs(
                ext.getDomains(),
                config.getProtocol(),
                appConfig.getHttpProxyPort(),
                appConfig.getHttpsProxyPort());
        Message.RuntimeInfo runtimeInfo = RuntimeInfoSupport.buildRuntimeInfo(config, remoteAddrs);
        if (create) {
            proxyConfigSyncService.syncOnCreate(config.getAgentId(), runtimeInfo);
        } else {
            proxyConfigSyncService.syncOnUpdate(config.getAgentId(), runtimeInfo);
        }
    }

    private void applyHealthCheckLifecycle(ProxyConfig config) {
        HealthCheckConfig healthCheck = config.getHealthCheck();
        if (config.isUdp() || healthCheck == null || !healthCheck.isEnabled()) {
            healthManager.removeProxy(config.getProxyId());
            logger.debug("健康检查已关闭或不适用于 UDP，清除代理 {} 的运行时健康状态", config.getProxyId());
        }
    }
}
