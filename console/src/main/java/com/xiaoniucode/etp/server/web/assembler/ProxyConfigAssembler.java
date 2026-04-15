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

package com.xiaoniucode.etp.server.web.assembler;

import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.*;
import com.xiaoniucode.etp.server.web.param.bandwidth.BandwidthSaveParam;
import com.xiaoniucode.etp.server.web.param.loadbalance.LoadBalanceParam;
import com.xiaoniucode.etp.server.web.param.proxy.HttpProxyCreateParam;
import com.xiaoniucode.etp.server.web.param.proxy.HttpProxyUpdateParam;
import com.xiaoniucode.etp.server.web.param.proxy.TcpProxyCreateParam;
import com.xiaoniucode.etp.server.web.param.proxy.TcpProxyUpdateParam;
import com.xiaoniucode.etp.server.web.param.proxytarget.ProxyTargetAddParam;
import com.xiaoniucode.etp.server.web.param.transport.TransportSaveParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class ProxyConfigAssembler {

    public ProxyConfig toDomain(HttpProxyCreateParam param) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setAgentId(param.getAgentId());
        proxyConfig.setAgentType(AgentType.BINARY);
        proxyConfig.setName(param.getName());
        proxyConfig.setEnabled(Objects.equals(param.getStatus(), ProxyStatus.OPEN.getCode()));
        proxyConfig.setProtocol(ProtocolType.HTTP);

        // 转换目标服务
        addTargets(proxyConfig, param.getTargets());

        // 转换带宽配置
        setBandwidthConfig(proxyConfig, param.getBandwidth());

        // 转换负载均衡配置
        setLoadBalanceConfig(proxyConfig, param.getLoadBalance());

        // 转换传输配置
        setTransportConfig(proxyConfig, param.getTransport());

        // 转换路由配置
        DomainType domainType = DomainType.fromCode(param.getDomainType());
        setRouteConfig(proxyConfig, domainType, param.getDomains());
        return proxyConfig;
    }

    public ProxyConfig toDomain(TcpProxyCreateParam param) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setAgentId(param.getAgentId());
        proxyConfig.setName(param.getName());
        proxyConfig.setEnabled(Objects.equals(param.getStatus(), ProxyStatus.OPEN.getCode()));
        proxyConfig.setProtocol(ProtocolType.TCP);
        proxyConfig.setRemotePort(param.getRemotePort());

        // 转换目标服务
        addTargets(proxyConfig, param.getTargets());

        // 转换带宽配置
        setBandwidthConfig(proxyConfig, param.getBandwidth());

        // 转换负载均衡配置
        setLoadBalanceConfig(proxyConfig, param.getLoadBalance());

        // 转换传输配置
        setTransportConfig(proxyConfig, param.getTransport());

        return proxyConfig;
    }

    public ProxyConfig toDomain(HttpProxyUpdateParam param) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyId(param.getId());
        proxyConfig.setName(param.getName());
        proxyConfig.setEnabled(param.getStatus() == 1);
        proxyConfig.setProtocol(ProtocolType.HTTP);

        // 转换目标服务
        addTargets(proxyConfig, param.getTargets());

        // 转换带宽配置
        setBandwidthConfig(proxyConfig, param.getBandwidth());

        // 转换负载均衡配置
        setLoadBalanceConfig(proxyConfig, param.getLoadBalance());

        // 转换传输配置
        setTransportConfig(proxyConfig, param.getTransport());

        // 转换路由配置
        DomainType domainType = DomainType.fromCode(param.getDomainType());
        setRouteConfig(proxyConfig, domainType, param.getDomains());
        return proxyConfig;
    }

    public ProxyConfig toDomain(TcpProxyUpdateParam param) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyId(param.getId());
        proxyConfig.setName(param.getName());
        proxyConfig.setEnabled(param.getStatus() == 1);
        proxyConfig.setProtocol(ProtocolType.TCP);
        proxyConfig.setRemotePort(param.getRemotePort());

        // 转换目标服务
        addTargets(proxyConfig, param.getTargets());

        // 转换带宽配置
        setBandwidthConfig(proxyConfig, param.getBandwidth());

        // 转换负载均衡配置
        setLoadBalanceConfig(proxyConfig, param.getLoadBalance());

        // 转换传输配置
        setTransportConfig(proxyConfig, param.getTransport());

        return proxyConfig;
    }

    /**
     * 添加目标服务
     */
    private void addTargets(ProxyConfig proxyConfig, List<ProxyTargetAddParam> targets) {
        if (targets != null) {
            for (ProxyTargetAddParam targetParam : targets) {
                Target target = new Target();
                target.setHost(targetParam.getHost());
                target.setPort(targetParam.getPort());
                target.setWeight(targetParam.getWeight() != null ? targetParam.getWeight() : 1);
                target.setName(targetParam.getName());
                proxyConfig.addTarget(target);
            }
        }
    }

    /**
     * 设置带宽配置
     */
    private void setBandwidthConfig(ProxyConfig proxyConfig, BandwidthSaveParam bandwidthParam) {
        if (bandwidthParam != null) {
            BandwidthConfig bandwidthConfig = new BandwidthConfig(
                    bandwidthParam.getLimitTotal(),
                    bandwidthParam.getLimitIn(),
                    bandwidthParam.getLimitOut()
            );
            proxyConfig.setBandwidth(bandwidthConfig);
        }
    }

    /**
     * 设置负载均衡配置
     */
    private void setLoadBalanceConfig(ProxyConfig proxyConfig, LoadBalanceParam loadBalanceParam) {
        if (loadBalanceParam != null) {
            LoadBalanceConfig loadBalanceConfig = new LoadBalanceConfig();
            LoadBalanceType strategy = LoadBalanceType.fromCode(loadBalanceParam.getStrategy());
            if (strategy != null) {
                loadBalanceConfig.setStrategy(strategy);
            }
            proxyConfig.setLoadBalance(loadBalanceConfig);
        }
    }

    /**
     * 设置传输配置
     */
    private void setTransportConfig(ProxyConfig proxyConfig, TransportSaveParam transportParam) {
        if (transportParam != null) {
            TransportCustomConfig transportConfig = new TransportCustomConfig();
            transportConfig.setEncrypt(transportParam.getEncrypt() != null ? transportParam.getEncrypt() : true);
            transportConfig.setMultiplex(Objects.equals(transportParam.getTunnelType(), TunnelType.MULTIPLEX.getCode()));
            proxyConfig.setTransport(transportConfig);
        }
    }

    /**
     * 设置路由配置
     */
    private void setRouteConfig(ProxyConfig proxyConfig, DomainType domainType, Set<String> domains) {
        RouteConfig routeConfig = new RouteConfig();
        if (domains != null && !domains.isEmpty()) {
            if (domainType == DomainType.AUTO) {
                routeConfig.setAutoDomain(true);
            } else if (domainType == DomainType.CUSTOM_DOMAIN) {
                routeConfig.getCustomDomains().addAll(domains);
            } else if (domainType == DomainType.SUBDOMAIN) {
                routeConfig.getSubDomains().addAll(domains);
            }
        }
        proxyConfig.setRouteConfig(routeConfig);
    }
}
