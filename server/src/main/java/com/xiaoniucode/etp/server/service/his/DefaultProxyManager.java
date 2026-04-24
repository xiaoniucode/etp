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

package com.xiaoniucode.etp.server.service.his;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.security.IpAccessChecker;
import com.xiaoniucode.etp.server.service.ConfigChangeDetector;
import com.xiaoniucode.etp.server.service.handler.ConfigHandler;
import com.xiaoniucode.etp.server.service.handler.ConfigHandlerFactory;
import com.xiaoniucode.etp.server.store.DomainStore;
import com.xiaoniucode.etp.server.vhost.DomainBinding;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.javers.core.diff.Diff;

import java.util.List;
import java.util.Optional;

public class DefaultProxyManager implements ProxyManager {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProxyManager.class);

    private final ConfigHandlerFactory configHandlerFactory;
    private final ConfigChangeDetector configChangeDetector;
    private final DomainStore domainStore;
    private final MetricsCollector metricsCollector;
    private final IpAccessChecker ipAccessChecker;

    public DefaultProxyManager(MetricsCollector metricsCollector,
                               DomainStore domainStore, ConfigChangeDetector configChangeDetector,
                               ConfigHandlerFactory configHandlerFactory, IpAccessChecker ipAccessChecker) {
        this.metricsCollector = metricsCollector;
        this.domainStore = domainStore;
        this.configHandlerFactory = configHandlerFactory;
        this.configChangeDetector = configChangeDetector;
        this.ipAccessChecker = ipAccessChecker;
    }

    /**
     *
     * @param proxyConfig
     * @return 如果不存在则返回新的，如果存在则更新后返回旧的。
     * @throws EtpException
     */
    @Override
    public synchronized RegisterResult register(ProxyConfig proxyConfig) throws EtpException {
        String agentId = proxyConfig.getAgentId();


        Optional<ProxyConfig> existing = findByAgentIdAndName(agentId, proxyConfig.getName());
        ConfigHandler delegate = configHandlerFactory.getHandler(proxyConfig);
        if (existing.isPresent()) {
            ProxyConfig oldConfig = existing.get();
            Diff diff = configChangeDetector.detectChanges(oldConfig, proxyConfig);
            if (diff.hasChanges()) {
                logger.debug("客户端 {} 代理配置 {} 信息变更", agentId, oldConfig.getName());
                return updateProxy(oldConfig, proxyConfig, delegate, diff);
            } else {
                RegisterResult registerResult = new RegisterResult();
                registerResult.setProxyConfig(oldConfig);
                registerResult.setListenPort(oldConfig.getListenPort());
                registerResult.setDomainBindings(domainStore.findByProxyId(oldConfig.getProxyId()));
                return registerResult;
            }
        } else {
            return createProxy(proxyConfig, delegate);
        }
    }

    private RegisterResult updateProxy(ProxyConfig oldConfig, ProxyConfig newConfig, ConfigHandler delegate, Diff diff) throws EtpException {
        //参数校验
        delegate.validate(newConfig);
        //重新注册
        RegisterResult registerResult = delegate.handReregister(oldConfig, newConfig, diff);
        //删除IP访问控制
        ipAccessChecker.invalidate(oldConfig.getProxyId());
        //替换
        return registerResult;
    }

    private RegisterResult createProxy(ProxyConfig newConfig, ConfigHandler delegate) throws EtpException {
        //参数校验
        delegate.validate(newConfig);
        //执行注册
        RegisterResult registerResult = delegate.handRegister(newConfig);
        return registerResult;
    }


    @Override
    public synchronized Optional<ProxyConfig> remove(String proxyId) {
        Optional<ProxyConfig> opt = findById(proxyId);
        if (opt.isPresent()) {
            ProxyConfig proxyConfig = opt.get();
            ConfigHandler delegate = configHandlerFactory.getHandler(proxyConfig);
            //删除IP访问控制
            ipAccessChecker.invalidate(proxyId);
            //释放代理相关资源信息
            delegate.unregister(proxyConfig);
            //删除代理流量统计记录
            metricsCollector.removeByProxyId(proxyId);
            return Optional.of(proxyConfig);
        }
        return Optional.empty();

    }

    @Override
    public synchronized void clearByAgentId(String agentId) {
        List<String> proxyIds = proxyStore.findProxyIdsByAgentId(agentId);
        for (String proxyId : proxyIds) {
            remove(proxyId);
        }
    }

    @Override
    public Optional<ProxyConfig> findByRemotePort(Integer port) {
        return Optional.ofNullable(proxyStore.findByRemotePort(port));
    }

    @Override
    public List<String> findProxyIdsByAgentId(String agentId) {
        return proxyStore.findProxyIdsByAgentId(agentId);
    }

    @Override
    public Optional<ProxyConfig> findById(String proxyId) {
        return Optional.ofNullable(proxyStore.findById(proxyId));
    }

    @Override
    public Optional<ProxyConfig> findByDomain(String domain) {
        DomainBinding domainBinding = domainStore.findByDomain(domain);
        if (domainBinding == null) {
            return Optional.empty();
        }
        String proxyId = domainBinding.getProxyId();
        return Optional.ofNullable(proxyStore.findById(proxyId));
    }

    public Optional<ProxyConfig> findByAgentIdAndName(String agentId, String proxyName) {
        return Optional.ofNullable(proxyStore.findByAgentIdAndName(agentId, proxyName));
    }

    @Override
    public synchronized ProxyConfig changeStatus(String proxyId, boolean enabled) {
        Optional<ProxyConfig> opt = findById(proxyId);
        if (opt.isPresent()) {
            ProxyConfig proxyConfig = opt.get();
            if (proxyConfig.isEnabled() != enabled) {
                proxyConfig.setEnabled(enabled);
                ConfigHandler delegate = configHandlerFactory.getHandler(proxyConfig);
                delegate.statusChanged(proxyConfig, enabled);
            }
            return proxyConfig;
        }

        return null;
    }

    @Override
    public void batchRemove(List<String> ids) {
        for (String proxyId : ids) {
            remove(proxyId);
        }
    }
}
