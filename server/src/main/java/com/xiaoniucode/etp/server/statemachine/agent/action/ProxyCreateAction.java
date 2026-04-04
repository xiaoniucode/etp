package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.google.protobuf.ProtocolStringList;
import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.*;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentConstants;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.vhost.DomainBinding;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProxyCreateAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyCreateAction.class);
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private DomainManager domainManager;
    @Resource
    private AppConfig appConfig;


    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Channel control = context.getControl();
        try {
            Message.NewProxy proxy = context.getAndRemoveAs(AgentConstants.NEWA_PROXY, Message.NewProxy.class);
            String agentId = context.getAgentInfo().getAgentId();
            ProxyConfig config = buildProxyConfig(proxy);
            logger.debug("{}", config);
            config.setAgentId(agentId);
            config.setAgentType(context.getAgentInfo().getAgentType());

            ProxyConfig register = proxyManager.register(config);
            Message.NewProxyResp newProxyResp = buildResponse(register);
            ByteBuf payload = ProtobufUtil.toByteBuf(newProxyResp, control.alloc());
            TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_PROXY_CREATE_RESP, payload);
            control.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()){
                    logger.debug("代理配置 {} 创建结果发送成功",register.getName());
                }else {
                    logger.error("代理配置 {} 创建失败",register.getName(),future.cause());
                }
            });
            context.fireEvent(AgentEvent.REBUILD_CONTEXT);
            logger.debug("代理注册成功: {}", register);
        } catch (Exception e) {
            logger.error("代理配置注册失败", e);
            sendErrorMessage(e.getMessage(), control);
        }
    }

    public void sendErrorMessage(String message, Channel control) {
        Message.Error msg = Message.Error.newBuilder().setMessage(message).build();
        ByteBuf payload = ProtobufUtil.toByteBuf(msg, control.alloc());
        TMSPFrame frame = new TMSPFrame(TMSP.MSG_ERROR, payload);
        control.writeAndFlush(frame);
    }

    private ProxyConfig buildProxyConfig(Message.NewProxy proxy) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setName(proxy.getName());
        List<Target> targets = proxy.getTargetsList().stream().map(p -> {
            Target target = new Target();
            target.setHost(p.getHost());
            target.setPort(p.getPort());
            if (p.hasName()) {
                target.setName(p.getName());
            }
            if (p.hasWeight()) {
                target.setWeight(p.getWeight());
            }
            return target;
        }).toList();
        proxyConfig.addTargets(targets);
        if (proxy.hasRemotePort()) {
            proxyConfig.setRemotePort(proxy.getRemotePort());
        }
        proxyConfig.setProtocol(ProtocolType.getByName(proxy.getProtocol().name()));
        if (proxy.hasEnable()) {
            proxyConfig.setEnabled(proxy.getEnable());
        }
        if (proxy.hasDomain()) {
            Message.DomainInfo domainInfo = proxy.getDomain();
            RouteConfig routeConfig = new RouteConfig();
            if (domainInfo.hasAutoDomain()) {
                routeConfig.setAutoDomain(domainInfo.getAutoDomain());
            }
            ProtocolStringList customDomainsList = domainInfo.getCustomDomainsList();
            if (!customDomainsList.isEmpty()) {
                routeConfig.getCustomDomains().addAll(customDomainsList);
            }
            ProtocolStringList subDomainsList = domainInfo.getSubDomainsList();
            if (!subDomainsList.isEmpty()) {
                routeConfig.getSubDomains().addAll(subDomainsList);
            }
            proxyConfig.setRouteConfig(routeConfig);
        }
        if (proxy.hasTransport()) {
            TransportCustomConfig transportCustomConfig = new TransportCustomConfig();
            Message.Transport transport = proxy.getTransport();
            if (transport.hasMux()) {
                transportCustomConfig.setMultiplex(transport.getMux());
            }
            if (transport.hasCompress()) {
                transportCustomConfig.setCompress(transport.getCompress());
            }
            if (transport.hasEncrypt()) {
                transportCustomConfig.setEncrypt(transport.getEncrypt());
            }
            proxyConfig.setTransport(transportCustomConfig);
        }

        if (proxy.hasAccessControl()) {
            Message.AccessControl accessControl = proxy.getAccessControl();
            boolean enable = accessControl.getEnable();
            AccessControlMode accessControlMode = AccessControlMode.fromValue(accessControl.getMode().name());
            Set<String> allow = new HashSet<>(accessControl.getAllowList());
            Set<String> deny = new HashSet<>(accessControl.getDenyList());
            proxyConfig.setAccessControl(new AccessControlConfig(enable, accessControlMode, allow, deny));
        }
        if (proxyConfig.getProtocol().isHttp() && proxy.hasBasicAuth()) {
            Message.BasicAuth basicAuth = proxy.getBasicAuth();
            Set<HttpUser> users = basicAuth.getHttpUsersList().stream()
                    .map(httpUser -> new HttpUser(httpUser.getUser(), httpUser.getPass()))
                    .collect(Collectors.toSet());
            BasicAuthConfig basicAuthConfig = new BasicAuthConfig(basicAuth.getEnable(), users);
            proxyConfig.setBasicAuth(basicAuthConfig);
        }
        if (proxy.hasBandwidth()) {
            Message.Bandwidth bandwidth = proxy.getBandwidth();
            BandwidthConfig bandwidthConfig = new BandwidthConfig(bandwidth.getLimit(),
                    bandwidth.getLimitIn(),
                    bandwidth.getLimitOut());
            proxyConfig.setBandwidth(bandwidthConfig);
        }
        if (proxy.hasLoadBalance()) {
            LoadBalanceConfig loadBalanceConfig = new LoadBalanceConfig();
            Message.LoadBalance loadBalance = proxy.getLoadBalance();
            if (loadBalance.hasStrategy()) {
                loadBalanceConfig.setStrategy(toJavaType(loadBalance.getStrategy()));
            }
            proxyConfig.setLoadBalance(loadBalanceConfig);
        }
        return proxyConfig;
    }

    private LoadBalanceStrategy toJavaType(Message.LoadBalanceStrategy strategy) {
        if (strategy == null) {
            return LoadBalanceConfig.DEFAULT_STRATEGY;
        }
        return switch (strategy) {
            case WEIGHT -> LoadBalanceStrategy.WEIGHT;
            case RANDOM -> LoadBalanceStrategy.RANDOM;
            case LEAST_CONN -> LoadBalanceStrategy.LEAST_CONN;
            default -> LoadBalanceConfig.DEFAULT_STRATEGY;
        };
    }

    private Message.NewProxyResp buildResponse(ProxyConfig config) {
        ProtocolType protocol = config.getProtocol();
        StringBuilder remoteAddr = new StringBuilder();
        if (protocol.isHttp()) {
            List<DomainBinding> domains = domainManager.getBoundDomains(config.getProxyId());
            if (domains != null) {
                if (ProtocolType.isHttp(protocol)) {
                    int httpProxyPort = appConfig.getHttpProxyPort();
                    for (DomainBinding domain : domains) {
                        remoteAddr.append("http://").append(domain.getDomain());
                        if (httpProxyPort != 80) {
                            remoteAddr.append(":").append(httpProxyPort);
                        }
                        remoteAddr.append("\n");
                    }
                }
            }

        } else if (protocol.isTcp()) {
            String serverAddr = appConfig.getServerAddr();
            Integer listenPort = config.getListenPort();
            remoteAddr.append(serverAddr).append(":").append(listenPort);
        }
        return Message.NewProxyResp.newBuilder()
                .setProxyId(config.getProxyId())
                .setProxyName(config.getName())
                .setRemoteAddr(remoteAddr.toString()).build();
    }
}
