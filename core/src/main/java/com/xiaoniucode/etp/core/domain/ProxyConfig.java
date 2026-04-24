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
package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.enums.ProtocolType;

import com.xiaoniucode.etp.core.enums.ProxySourceType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import lombok.*;
import org.javers.core.metamodel.annotation.DiffIgnore;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@ToString
@EqualsAndHashCode
public class ProxyConfig implements Serializable {
    @Setter
    private String agentId;
    @Setter
    private AgentType agentType;
    /**
     * 代理ID 唯一标识
     */
    @Setter
    @DiffIgnore
    private String proxyId;
    /**
     * 代理名称
     */
    @Setter
    @DiffIgnore
    private String name;
    /**
     * 协议类型
     */
    @Setter
    private ProtocolType protocol;
    /**
     * 代理配置来源
     */
    @Setter
    private ProxySourceType sourceType;
    /**
     * TCP 代理 远程端口
     */
    @Setter
    private Integer remotePort;
    /**
     * 如果 remotePort 未指定则自动生成，listenPort为实际端口
     */
    @DiffIgnore
    @Setter
    private Integer listenPort;
    /**
     * 代理目标服务
     */
    private final List<Target> targets = new CopyOnWriteArrayList<>();
    /**
     * 代理状态
     */
    @Setter
    private ProxyStatus status;
    /**
     * HTTP(s) 域名配置
     */
    @Setter
    private RouteConfig routeConfig;
    /**
     * IP 防火墙
     */
    @Setter
    private AccessControlConfig accessControl;
    /**
     * HTTP Basic Auth配置
     */
    @Setter
    private BasicAuthConfig basicAuth;
    /**
     * 带宽限制配置
     */
    @Setter
    private BandwidthConfig bandwidth;
    /**
     * 负载均衡配置
     */
    @Setter
    private LoadBalanceConfig loadBalance;
    /**
     * 传输配置
     */
    @Setter
    private TransportCustomConfig transport;
    /**
     * 健康检查
     */
    @Setter
    private HealthCheckConfig healthCheck;

    public boolean hasTransport() {
        return transport != null;
    }

    /**
     * 是否启用加密
     */
    public boolean isEncrypt() {
        return transport != null && Boolean.TRUE.equals(transport.getEncrypt());
    }

    /**
     * 是否启用压缩
     */
    public boolean isCompress() {
        return transport != null && Boolean.TRUE.equals(transport.getCompress());
    }

    public boolean hasAccessControl() {
        return accessControl != null;
    }

    public boolean hasBandwidthLimit() {
        return bandwidth != null;
    }

    public boolean hasBasicAuth() {
        return basicAuth != null;
    }

    /**
     * 是否是 HTTP 协议
     */
    public boolean isHttp() {
        return ProtocolType.isHttp(protocol);
    }

    /**
     * 是否是 TCP 协议
     */
    public boolean isTcp() {
        return ProtocolType.isTcp(protocol);
    }

    public boolean hasRemotePort() {
        return remotePort != null;
    }

    public boolean hasLoadBalance() {
        return loadBalance != null;
    }

    /**
     * 是否需要使用负载均衡
     */
    public boolean isLoadBalanceNeeded() {
        return targets.size() > 1;
    }

    public boolean addTarget(Target target) {
        if (target == null) {
            return false;
        }
        if (targets.contains(target)) {
            return false;
        }
        targets.add(target);
        return true;
    }

    public int addTargets(List<Target> newTargets) {
        if (newTargets == null || newTargets.isEmpty()) {
            return 0;
        }
        int addedCount = 0;
        for (Target target : newTargets) {
            if (addTarget(target)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    public BasicAuthConfig getOrCreateBasicAuthConfig(){
        if (basicAuth==null){
            return new BasicAuthConfig();
        }
        return basicAuth;
    }

    public boolean isMuxTunnel() {
        return transport != null && transport.getMultiplex();
    }

    public AccessControlConfig getOrCreateAccessControlConfig() {
        if (accessControl==null){
            return new AccessControlConfig();
        }
        return accessControl;
    }
}

