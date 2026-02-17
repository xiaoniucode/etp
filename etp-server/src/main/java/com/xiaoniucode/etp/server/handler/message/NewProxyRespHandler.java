package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.core.handler.MessageHandler;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.event.ProxyCreatedEvent;
import com.xiaoniucode.etp.server.generator.GlobalIdGenerator;
import com.xiaoniucode.etp.server.handler.utils.MessageUtils;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.PortListenerManager;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Autowired
    private PortListenerManager portListenerManager;
    @Resource
    private AppConfig appConfig;

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        Channel control = ctx.channel();
        agentSessionManager.getAgentSession(control).ifPresent(agent -> {
            String clientId = agent.getClientId();
            Message.NewProxy newProxy = msg.getNewProxy();
            ClientType clientType = agent.getClientType();
            ProxyConfig config = buildProxyConfig(newProxy);
            //判断代理是否已经存在了
            if (proxyManager.hasProxy(clientId, config)) {
                logger.warn("代理配置已经存在，跳过注册：[客户端标识={}，代理名称={}]", clientId, config.getName());
                return;
            }
            //保存到代理到配置管理器
            proxyManager.addProxy(clientId, config, proxyConfig -> {
                //发布事件，可订阅事件对其进行持久化或其他操作
                //todo test
                if (ProtocolType.isTcp(proxyConfig.getProtocol())) {
                    Integer remotePort = proxyConfig.getRemotePort();
                    portListenerManager.bindPort(remotePort);
                    agentSessionManager.addPortToAgentSession(remotePort);
                }
                if (ProtocolType.isHttp(proxyConfig.getProtocol())) {
                    Set<String> domains = proxyConfig.getFullDomains();
                    agentSessionManager.addDomainsToAgentSession(domains);
                }
                //注册代理配置
                eventBus.publishAsync(new ProxyCreatedEvent(clientId, clientType, proxyConfig));
                Message.ControlMessage controlMessage = buildResponse(proxyConfig);
                control.writeAndFlush(controlMessage);
                logger.debug("代理注册成功: [代理名称={}]", proxyConfig.getName());
            });
        });

    }

    private ProxyConfig buildProxyConfig(Message.NewProxy proxy) {
        String proxyId = GlobalIdGenerator.uuid32();
        ProxyConfig config = new ProxyConfig();
        config.setProxyId(proxyId);
        config.setName(proxy.getName());
        config.setLocalIp(proxy.getLocalIp());
        config.setLocalPort(proxy.getLocalPort());
        config.setRemotePort(proxy.getRemotePort());
        config.setProtocol(ProtocolType.getByName(proxy.getProtocol().name()));
        config.setStatus(ProxyStatus.fromStatus(proxy.getStatus()));
        config.setAutoDomain(proxy.getAutoDomain());
        config.setCompress(proxy.getCompress());
        config.setEncrypt(proxy.getEncrypt());
        config.getCustomDomains().addAll(proxy.getCustomDomainsList());
        config.getSubDomains().addAll(proxy.getSubDomainsList());
        return config;
    }

    private Message.ControlMessage buildResponse(ProxyConfig config) {
        ProtocolType protocol = config.getProtocol();
        Set<String> domains = config.getFullDomains();

        if (domains == null || domains.isEmpty()) {
            //todo return error
            return null;
        }
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
