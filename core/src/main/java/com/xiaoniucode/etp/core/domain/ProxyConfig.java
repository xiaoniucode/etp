package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.ProtocolType;

import lombok.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@ToString
public class ProxyConfig {
    @Setter
    private String clientId;
    @Setter
    private ClientType clientType;
    /**
     * 代理ID 唯一标识
     */
    @Setter
    private String proxyId;
    /**
     * 代理名称
     */
    @Setter
    private String name;
    /**
     * 协议类型
     */
    @Setter
    private ProtocolType protocol;
    /**
     * TCP 代理 远程端口
     */
    @Setter
    private Integer remotePort;
    /**
     * 代理目标服务
     */
    private final List<Target> targets = new CopyOnWriteArrayList<>();
    /**
     * 是否开启代理
     */
    @Setter
    private boolean enabled = true;
    /**
     * HTTP(s) 域名配置
     */
    @Setter
    private DomainConfig domainInfo;
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
    private TransportConfig transport;
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
     * 是否需要负载均衡
     * 条件:
     * 1. 有多个target
     * 2. 没有配置负载均衡时，默认启用轮询
     * 3. 配置了负载均衡时，按配置的策略
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

    public boolean isMuxTunnel() {
        return transport != null && transport.getMultiplex();
    }
}

