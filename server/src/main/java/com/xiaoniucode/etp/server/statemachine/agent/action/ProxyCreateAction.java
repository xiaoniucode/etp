package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.google.protobuf.ProtocolStringList;
import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.*;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.valid.ValidInfo;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.utils.CommandMessageUtils;
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
//    @Autowired
//    private ProxyConfigProcessorExecutor executor;
    @Autowired
    private EventBus eventBus;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private UUIDGenerator uuidGenerator;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

        Message.NewProxy proxy = context.getVariableAs("newProxy", Message.NewProxy.class);
        Channel control = context.getControl();
        String clientId = context.getClientId();
        ClientType clientType = context.getClientType();

        ProxyConfig config = buildProxyConfig(proxy);
        ValidInfo validInfo = proxyManager.validProxy(clientId, config);
//        if (validInfo.isInValid()) {
//            logger.warn("无效配置：[客户端标识={}，代理名称={}]", clientId, config.getName());
//            control.writeAndFlush(CommandMessageUtils.buildErrorMessage(400, validInfo.getMessage()));
//            return;
//        }
//        if (validInfo.isUpdate()) {
//            proxyManager.removeProxyByName(clientId, config.getName());
//        }
//        control.writeAndFlush(buildResponse(config));
//        proxyManager.addProxy(clientId, config, proxyConfig -> {
//           // executor.execute(proxyConfig);
//            if (validInfo.isNew()) {
//                eventBus.publishAsync(new ProxyCreatedEvent(clientId, clientType, proxyConfig));
//            }
//            if (validInfo.isUpdate()) {
//                eventBus.publishAsync(new ProxyUpdatedEvent(clientId, clientType, proxyConfig));
//            }
//            control.writeAndFlush(buildResponse(proxyConfig));
//            logger.debug("代理注册成功: [代理名称={}]", proxyConfig.getName());
//        });
    }

    private ProxyConfig buildProxyConfig(Message.NewProxy proxy) {
        String proxyId = uuidGenerator.uuid32();

        ProxyConfig config = new ProxyConfig();
        config.setProxyId(proxyId);
        config.setName(proxy.getName());
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
        config.addTargets(targets);
        if (proxy.hasRemotePort()) {
            config.setRemotePort(proxy.getRemotePort());
        }
        config.setProtocol(ProtocolType.getByName(proxy.getProtocol().name()));
        if (proxy.hasStatus()) {
            config.setStatus(ProxyStatus.fromStatus(proxy.getStatus()));
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
        }
        if (proxy.hasTransport()) {
            TransportConfig transportConfig = new TransportConfig();
            Message.Transport transport = proxy.getTransport();
            if (transport.hasMux()) {
                transportConfig.setMux(transport.getMux());
            }
            if (transport.hasProtocol()) {
                Message.TransportProtocolType protocol = transport.getProtocol();
                TransportProtocol tp = TransportProtocol.fromName(protocol.getValueDescriptor().getName());
                transportConfig.setProtocol(tp);
            }
            if (transport.hasCompress()) {
                Message.Compress compress = transport.getCompress();
                CompressionConfig compressionConfig = new CompressionConfig();
                compressionConfig.setEnable(compress.getEnable());
                transportConfig.setCompress(compressionConfig);
            }
            if (transport.hasEncrypt()) {
                EncryptionConfig encryptionConfig = new EncryptionConfig();
                Message.Encrypt encrypt = transport.getEncrypt();
                encryptionConfig.setEnable(encrypt.getEnable());
                transportConfig.setEncrypt(encryptionConfig);
            }
        }


        if (proxy.hasAccessControl()) {
            Message.AccessControl accessControl = proxy.getAccessControl();
            boolean enable = accessControl.getEnable();
            AccessControlMode accessControlMode = AccessControlMode.fromValue(accessControl.getMode().name());
            Set<String> allow = new HashSet<>(accessControl.getAllowList());
            Set<String> deny = new HashSet<>(accessControl.getDenyList());
            config.setAccessControl(new AccessControlConfig(enable, accessControlMode, allow, deny));
        }
        if (config.getProtocol().isHttp() && proxy.hasBasicAuth()) {
            Message.BasicAuth basicAuth = proxy.getBasicAuth();
            Set<HttpUser> users = basicAuth.getHttpUsersList().stream()
                    .map(httpUser -> new HttpUser(httpUser.getUser(), httpUser.getPass()))
                    .collect(Collectors.toSet());
            BasicAuthConfig basicAuthConfig = new BasicAuthConfig(basicAuth.getEnable(), users);
            config.setBasicAuth(basicAuthConfig);
        }
        if (proxy.hasBandwidth()) {
            Message.Bandwidth bandwidth = proxy.getBandwidth();
            BandwidthConfig bandwidthConfig = new BandwidthConfig(bandwidth.getLimit(),
                    bandwidth.getLimitIn(),
                    bandwidth.getLimitOut());
            config.setBandwidth(bandwidthConfig);
        }
        if (proxy.hasLoadBalance()) {
            LoadBalanceConfig loadBalanceConfig = new LoadBalanceConfig();
            Message.LoadBalance loadBalance = proxy.getLoadBalance();
            if (loadBalance.hasStrategy()) {
                loadBalanceConfig.setStrategy(toJavaType(loadBalance.getStrategy()));
            }
            config.setLoadBalance(loadBalanceConfig);
        }
        return config;
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

    private Message.ConfigMessage buildResponse(ProxyConfig config) {
        ProtocolType protocol = config.getProtocol();
        Set<String> domains = config.getDomainInfo().getFullDomains();
        String host = appConfig.getServerAddr();
        StringBuilder remoteAddr = new StringBuilder();
        if (ProtocolType.isHttp(protocol)) {
            int httpProxyPort = appConfig.getHttpProxyPort();
            for (String domain : domains) {
                remoteAddr.append("http://").append(domain);
                if (httpProxyPort != 80) {
                    remoteAddr.append(":").append(httpProxyPort);
                }
                remoteAddr.append("\n");
            }

        } else if (ProtocolType.isTcp(protocol)) {
            Integer remotePort = config.getRemotePort();
            remoteAddr.append(host).append(":").append(remotePort);
        }
        return CommandMessageUtils.buildNewProxyResp(config.getName(), remoteAddr.toString());
    }
}
