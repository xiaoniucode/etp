package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancerFactory;
import com.xiaoniucode.etp.server.proxy.ProxyManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.loadbalance.HealthManager;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

@Component
public class TargetResolverAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(TargetResolverAction.class);
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private LoadBalancerFactory loadBalancerFactory;

    @Autowired
    private AgentManager agentManager;
    @Autowired
    private HealthManager healthManager;

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        Channel visitor = context.getVisitor();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        ProxyConfig config = resolveProxyConfig(context);
        if (config == null || !config.isEnable()) {
            logger.debug("代理不可用");
            context.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }

        Optional<AgentContext> gentContextOpt = agentManager.getAgentContextByProxyId(config.getProxyId());
        if (gentContextOpt.isPresent()) {
            context.setProxyConfig(config);
            context.setAgentContext(gentContextOpt.get());
            Target selectedTarget = selectTarget(config, context);
            if (selectedTarget == null) {
                logger.warn("无可用的后端目标: proxyId={}", config.getProxyId());
                context.fireEvent(StreamEvent.STREAM_CLOSE);
                return;
            }
            BandwidthConfig bandwidth = config.getBandwidth();
            if (bandwidth != null) {
                context.setBandwidthLimiter(new BandwidthLimiter(bandwidth));
            }
            context.setCurrentTarget(selectedTarget);
            context.fireEvent(StreamEvent.TARGET_VALIDATED);
        } else {
            logger.debug("代理客户端不可用: proxyId={}", config.getProxyId());
            //客户端不可用
            context.fireEvent(StreamEvent.STREAM_CLOSE);
        }
    }

    private Target selectTarget(ProxyConfig config, StreamContext context) {
        List<Target> targets = config.getTargets();
        if (CollectionUtils.isEmpty(targets)) {
            return null;
        }
        List<Target> availableTargets = targets;
        HealthCheckConfig healthCheck = config.getHealthCheck();
        //获取健康目标服务列表
        if (healthCheck != null && healthCheck.isEnable()) {
            availableTargets = healthManager.getAvailableTargets(config.getProxyId(), targets);
        }
        if (CollectionUtils.isEmpty(availableTargets)) {
            logger.warn("没有可用的健康目标: proxy={}", config.getName());
            return null;
        }
        if (config.isLoadBalanceNeeded()) {
            LoadBalanceConfig loadBalanceConfig = config.getLoadBalance();
            LoadBalancer loadBalancer = loadBalancerFactory.getLoadBalancer(loadBalanceConfig);
            Target selected = loadBalancer.select(config.getProxyId(), availableTargets);
            if (selected != null) {
                logger.debug("负载均衡选择: {} -> {}:{}",
                        config.getName(), selected.getHost(), selected.getPort());
            }
            return selected;
        } else {
            Target selected = availableTargets.getFirst();
            logger.debug("单个目标选择: {} -> {}:{}",
                    config.getName(), selected.getHost(), selected.getPort());
            return selected;
        }
    }

    private ProxyConfig resolveProxyConfig(StreamContext context) {
        Channel visitor = context.getVisitor();
        if (context.getCurrentProtocol() == ProtocolType.HTTP) {
            String domain = visitor.attr(AttributeKeys.VISIT_DOMAIN).get();
            return proxyManager.findByDomain(domain).orElse(null);

        } else if (context.getCurrentProtocol() == ProtocolType.TCP) {
            int remotePort = getListenerPort(visitor);
            return proxyManager.findByRemotePort(remotePort).orElse(null);
        }
        return null;
    }

    private int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }
}