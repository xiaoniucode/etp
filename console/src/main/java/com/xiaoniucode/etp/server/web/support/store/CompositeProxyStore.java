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

import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.store.DomainStore;
import com.xiaoniucode.etp.server.store.ProxyStore;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import com.xiaoniucode.etp.server.web.dto.proxy.ProxyDetailQueryResult;
import com.xiaoniucode.etp.server.web.entity.*;
import com.xiaoniucode.etp.server.web.repository.*;
import com.xiaoniucode.etp.server.web.support.store.converter.ProxyStoreConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@Primary
public class CompositeProxyStore implements ProxyStore {
    private final Logger logger = LoggerFactory.getLogger(CompositeProxyStore.class);
    @Autowired
    private MultiLevelCache multiLevelCache;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ProxyDomainRepository proxyDomainRepository;
    @Autowired
    private ProxyTargetRepository proxyTargetRepository;
    @Autowired
    private AccessControlRepository accessControlRepository;
    @Autowired
    private AccessControlRuleRepository accessControlRuleRepository;
    @Autowired
    private BasicUserRepository basicUserRepository;
    @Autowired
    private ProxyStoreConvert proxyStoreConvert;
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private DomainStore domainStore;
    private final String CACHE_NAME = "proxy";

    @Override
    public ProxyConfig save(ProxyConfig config) {
        logger.debug("保存代理配置，代理ID: {}", config.getProxyId());
        clearProxyCache(config.getProxyId());
        return config;
    }

    @Override
    public ProxyConfig findById(String proxyId) {
        return multiLevelCache.getAndPut(CACHE_NAME, "id:" + proxyId, () -> {
            ProxyDetailQueryResult detail = proxyRepository.findProxyDetailByProxyId(proxyId);
            if (detail==null){
                return null;
            }
            return buildProxyConfig(detail);
        });
    }

    @Override
    public List<String> findProxyIdsByAgentId(String agentId) {
        logger.debug("根据代理ID查询代理列表，agentId: {}", agentId);
        String cacheKey = "agent:" + agentId;
        return multiLevelCache.getAndPut(CACHE_NAME, cacheKey, () ->
                proxyRepository.findProxyIdsByAgentId(agentId));
    }

    @Override
    public ProxyConfig findByRemotePort(Integer remotePort) {
        String proxyId = multiLevelCache.getAndPut(CACHE_NAME, "remotePort:" + remotePort, () ->
                proxyRepository.findByRemotePort(remotePort).map(ProxyDO::getId).orElse(null));
        return findById(proxyId);

    }

    @Override
    public void deleteById(String proxyId) {
        logger.debug("删除代理配置，代理ID: {}", proxyId);
        clearProxyCache(proxyId);
    }

    @Override
    public void deleteByAgentId(String agentId) {
        logger.debug("删除指定代理的所有配置，agentId: {}", agentId);
        //todo 需要清理相关的代理缓存
        multiLevelCache.evict(CACHE_NAME, "agent:" + agentId);
    }

    private void clearProxyCache(String proxyId) {
        multiLevelCache.evict(CACHE_NAME, "id:" + proxyId);
        multiLevelCache.evict(CACHE_NAME, "proxy:exists:id:" + proxyId);
    }

    @Override
    public boolean existsById(String proxyId) {
        if (proxyId == null) {
            return false;
        }

        Boolean exists = multiLevelCache.getAndPut(CACHE_NAME, "proxy:exists:id:" + proxyId,
                () -> proxyRepository.findById(proxyId).isPresent()
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public ProxyConfig findByAgentIdAndName(String agentId, String proxyName) {
        String cacheKey = String.format(
                "proxy::query::agent_name::%s::%s",
                agentId, proxyName
        );
        String proxyId = multiLevelCache.getAndPut(CACHE_NAME, cacheKey, () ->
                proxyRepository.findByAgentIdAndName(agentId, proxyName)
                        .map(ProxyDO::getId)
                        .orElse(null)
        );
        return findById(proxyId);
    }

    private ProxyConfig buildProxyConfig(ProxyDetailQueryResult detail) {
        AgentDO agentDO = detail.getAgentDO();
        ProxyDO proxyDO = detail.getProxyDO();
        TransportDO transportDO = detail.getTransportDO();
        BandwidthDO bandwidthDO = detail.getBandwidthDO();
        LoadBalanceDO loadBalanceDO = detail.getLoadBalanceDO();
        BasicAuthDO basicAuthDO = detail.getBasicAuthDO();

        String proxyId = proxyDO.getId();
        //基本信息
        ProxyConfig proxyConfig = proxyStoreConvert.toBaseDomain(proxyDO);
        proxyConfig.setAgentId(agentDO.getId());
        proxyConfig.setAgentType(agentDO.getAgentType());
        proxyConfig.setListenPort(proxyDO.getRemotePort());
        //服务列表
        List<ProxyTargetDO> proxyTargetDos = proxyTargetRepository.findByProxyId(proxyId);
        List<Target> targets = proxyStoreConvert.toTargetDomains(proxyTargetDos);
        proxyConfig.addTargets(targets);
        //负载均衡
        if (proxyDO.getDeploymentMode().isCluster()) {
            LoadBalanceConfig loadBalanceConfig = proxyStoreConvert.toLoadBalanceDomain(loadBalanceDO);
            proxyConfig.setLoadBalance(loadBalanceConfig);
        }
        //带宽
        proxyConfig.setBandwidth(proxyStoreConvert.toBandwidthDomain(bandwidthDO));
        //传输层
        proxyConfig.setTransport(proxyStoreConvert.toTransportDomain(transportDO));
        //IP访问控制
        accessControlRepository.findByProxyId(proxyId).ifPresent(accessControlDO -> {
            AccessControlConfig accessControlConfig = proxyStoreConvert.toAccessControlDomain(accessControlDO);
            List<AccessControlRuleDO> rules = accessControlRuleRepository.findByProxyId(accessControlDO.getProxyId());
            for (AccessControlRuleDO rule : rules) {
                switch (rule.getMode()) {
                    case ALLOW -> accessControlConfig.addAllow(rule.getCidr());
                    case DENY -> accessControlConfig.addDeny(rule.getCidr());
                }
            }
            proxyConfig.setAccessControl(accessControlConfig);
        });

        ProtocolType protocol = proxyDO.getProtocol();
        if (protocol.isHttp()) {
            // HTTP Basic Auth
            if (basicAuthDO != null) {
                BasicAuthConfig basicAuthConfig = proxyStoreConvert.toBasicAuthDomain(basicAuthDO);
                List<BasicUserDO> basicUsers = basicUserRepository.findByProxyId(basicAuthDO.getProxyId());
                Set<HttpUser> httpUsers = proxyStoreConvert.toBasicUserDomains(basicUsers);
                basicAuthConfig.addUsers(httpUsers);
                proxyConfig.setBasicAuth(basicAuthConfig);
            }
            // HTTP 域名
            List<HttpProxyDomainDO> httpProxyDomains = proxyDomainRepository.findByProxyId(proxyId);
            RouteConfig routeConfig = new RouteConfig();

            for (HttpProxyDomainDO domainDO : httpProxyDomains) {
                DomainType domainType = proxyDO.getDomainType();
                if (domainType.isCustomDomain()) {
                    routeConfig.addCustomDomain(domainDO.getDomain());
                } else if (domainType.isSubdomain()) {
                    routeConfig.addSubDomain(domainDO.getDomain());
                } else if (domainType.isAuto()) {
                    routeConfig.setAutoDomain(true);
                }
            }
            proxyConfig.setRouteConfig(routeConfig);
            //如果代理没有注册域名，则注册
            if (domainStore.findByProxyId(proxyId) == null) {
                domainManager.register(proxyId, routeConfig);
            }
        }
        return proxyConfig;
    }
}
