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
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProxyCreateAction extends AgentBaseAction {
    private final Logger logger = LoggerFactory.getLogger(ProxyCreateAction.class);
    @Autowired
    private ProxyManager proxyManager;
    @Resource
    private AppConfig appConfig;


    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Channel control = context.getControl();
        try {
            Message.NewProxy proxy = context.getAndRemoveAs(AgentConstants.NEWA_PROXY, Message.NewProxy.class);
            String clientId = context.getClientId();
            ProxyConfig config = buildProxyConfig(proxy);
            logger.debug("{}", config);
            config.setClientId(clientId);
            config.setClientType(context.getClientType());

            ProxyConfig register = proxyManager.register(config);
            Message.NewProxyResp newProxyResp = buildResponse(register);
            TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_PROXY_CREATE_RESP, ProtobufUtil.toByteBuf(newProxyResp, control.alloc()));
            control.writeAndFlush(frame);
            logger.debug("代理注册成功: {}", register);
        } catch (Exception e) {
            logger.error("代理配置注册失败", e);
            sendErrorMessage(e.getMessage(), control);
        } finally {
            context.removeVariable(AgentConstants.NEWA_PROXY);
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
            proxyConfig.setEnable(proxy.getEnable());
        }
        if (proxy.hasDomain()) {
            Message.DomainInfo domainInfo = proxy.getDomain();
            DomainConfig domainConfig = new DomainConfig();
            if (domainInfo.hasAutoDomain()) {
                domainConfig.setAutoDomain(domainInfo.getAutoDomain());
            }
            ProtocolStringList customDomainsList = domainInfo.getCustomDomainsList();
            if (!customDomainsList.isEmpty()) {
                domainConfig.getCustomDomains().addAll(customDomainsList);
            }
            ProtocolStringList subDomainsList = domainInfo.getSubDomainsList();
            if (!subDomainsList.isEmpty()) {
                domainConfig.getSubDomains().addAll(subDomainsList);
            }
            proxyConfig.setDomainInfo(domainConfig);
        }
        if (proxy.hasTransport()) {
            TransportConfig transportConfig = new TransportConfig();
            Message.Transport transport = proxy.getTransport();
            if (transport.hasMux()) {
                transportConfig.setMux(transport.getMux());
            }
            if (transport.hasCompress()) {
                transportConfig.setCompress(transport.getCompress());
            }
            if (transport.hasEncrypt()) {
                transportConfig.setEncrypt(transport.getEncrypt());
            }
            proxyConfig.setTransport(transportConfig);
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
            DomainConfig domainInfo = config.getDomainInfo();
            if (domainInfo != null) {
                Set<String> domains = domainInfo.getFullDomains();
                if (ProtocolType.isHttp(protocol)) {
                    int httpProxyPort = appConfig.getHttpProxyPort();
                    for (String domain : domains) {
                        remoteAddr.append("http://").append(domain);
                        if (httpProxyPort != 80) {
                            remoteAddr.append(":").append(httpProxyPort);
                        }
                        remoteAddr.append("\n");
                    }
                }
            }

        } else if (protocol.isTcp()) {
            String serverAddr = appConfig.getServerAddr();
            Integer remotePort = config.getRemotePort();
            remoteAddr.append(serverAddr).append(":").append(remotePort);
        }
        return Message.NewProxyResp.newBuilder()
                .setProxyId(config.getProxyId())
                .setProxyName(config.getName())
                .setRemoteAddr(remoteAddr.toString()).build();
    }
}
