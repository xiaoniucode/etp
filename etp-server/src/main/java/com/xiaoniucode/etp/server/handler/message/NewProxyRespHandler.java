package com.xiaoniucode.etp.server.handler.message;

import com.google.protobuf.ProtocolStringList;
import com.xiaoniucode.etp.core.domain.AccessControlConfig;
import com.xiaoniucode.etp.core.domain.BasicAuthConfig;
import com.xiaoniucode.etp.core.domain.HttpUser;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.core.handler.MessageHandler;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.event.ProxyCreatedEvent;
import com.xiaoniucode.etp.server.event.ProxyUpdatedEvent;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import com.xiaoniucode.etp.server.handler.utils.MessageUtils;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.domain.valid.ValidInfo;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.proxy.processor.ProxyConfigProcessorExecutor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.xiaoniucode.etp.core.message.Message.ControlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理来自代理客户端端口映射注册
 *
 * @author liuxin
 */
@Component
public class NewProxyRespHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewProxyRespHandler.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private AgentSessionManager agentSessionManager;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private ProxyConfigProcessorExecutor processorExecutor;

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        Channel control = ctx.channel();
        agentSessionManager.getAgentSession(control).ifPresent(agent -> {
            String clientId = agent.getClientId();
            ProxyConfig config = buildProxyConfig(msg.getNewProxy());
            ValidInfo validInfo = proxyManager.validProxy(clientId, config);
            if (validInfo.isInValid()) {
                logger.warn("无效配置：[客户端标识={}，代理名称={}]", clientId, config.getName());
                control.writeAndFlush(MessageUtils.buildErrorMessage(400, validInfo.getMessage()));
                return;
            }
            if (validInfo.isUpdate()) {
                proxyManager.removeProxyByName(clientId, config.getName());
            }
            proxyManager.addProxy(clientId, config, proxyConfig -> {
                processorExecutor.execute(proxyConfig);
                if (validInfo.isNew()) {
                    eventBus.publishAsync(new ProxyCreatedEvent(clientId, agent.getClientType(), proxyConfig));
                }
                if (validInfo.isUpdate()) {
                    eventBus.publishAsync(new ProxyUpdatedEvent(clientId, agent.getClientType(), proxyConfig));
                }
                control.writeAndFlush(buildResponse(proxyConfig));
                logger.debug("代理注册成功: [代理名称={}]", proxyConfig.getName());
            });
        });
    }

    private ProxyConfig buildProxyConfig(Message.NewProxy proxy) {
        String proxyId = GlobalIdGenerator.uuid32();

        ProxyConfig config = new ProxyConfig();
        config.setProxyId(proxyId);
        config.setName(proxy.getName());
        if (proxy.hasLocalIp()) {
            config.setLocalIp(proxy.getLocalIp());
        }
        config.setLocalPort(proxy.getLocalPort());
        if (proxy.hasRemotePort()) {
            config.setRemotePort(proxy.getRemotePort());
        }
        config.setProtocol(ProtocolType.getByName(proxy.getProtocol().name()));
        if (proxy.hasStatus()) {
            config.setStatus(ProxyStatus.fromStatus(proxy.getStatus()));
        }
        if (proxy.hasAutoDomain()) {
            config.setAutoDomain(proxy.getAutoDomain());
        }
        if (proxy.hasCompress()) {
            config.setCompress(proxy.getCompress());
        }
        if (proxy.hasEncrypt()) {
            config.setEncrypt(proxy.getEncrypt());
        }
        ProtocolStringList customDomainsList = proxy.getCustomDomainsList();
        if (!customDomainsList.isEmpty()) {
            config.getCustomDomains().addAll(customDomainsList);
        }
        ProtocolStringList subDomainsList = proxy.getSubDomainsList();
        if (!subDomainsList.isEmpty()) {
            config.getSubDomains().addAll(subDomainsList);
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
            List<Message.HttpUser> httpUsersList = basicAuth.getHttpUsersList();
            Set<HttpUser> users = new HashSet<>();
            for (Message.HttpUser httpUser : httpUsersList) {
                users.add(new HttpUser(httpUser.getUser(),httpUser.getPass()));
            }
            BasicAuthConfig basicAuthConfig = new BasicAuthConfig(basicAuth.getEnable(), users);
            config.setBasicAuth(basicAuthConfig);
        }
        return config;
    }

    private Message.ControlMessage buildResponse(ProxyConfig config) {
        ProtocolType protocol = config.getProtocol();
        Set<String> domains = config.getFullDomains();
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
        return MessageUtils.buildNewProxyResp(config.getName(), remoteAddr.toString());
    }
}
