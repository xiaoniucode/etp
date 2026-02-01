package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.core.handler.MessageHandler;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.event.ProxyRegisterEvent;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.TcpServerManager;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
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
    private TcpServerManager tcpServerManager;

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        Channel control = ctx.channel();
        agentSessionManager.getAgentSession(control).ifPresent(agent -> {
            String clientId = agent.getClientId();
            Message.NewProxy newProxy = msg.getNewProxy();
            ProxyConfig config = buildProxyConfig(newProxy);
            //保存到代理到配置管理器
            proxyManager.createProxy(clientId, config, proxyConfig -> {
                //发布事件，可订阅事件对其进行持久化或其他操作
                //todo test
                if (proxyConfig.getProtocol() == ProtocolType.TCP) {
                    Integer remotePort = proxyConfig.getRemotePort();
                    tcpServerManager.bindPort(remotePort);
                    agentSessionManager.addPortToAgentSession(remotePort);
                }
                //注册代理配置
                eventBus.publishAsync(new ProxyRegisterEvent(proxyConfig));
                control.writeAndFlush(buildResponse(proxyConfig));
                logger.debug("代理注册成功: [代理名称={}]", proxyConfig.getName());
            });
        });

    }

    private ProxyConfig buildProxyConfig(Message.NewProxy proxy) {
        ProxyConfig config = new ProxyConfig();
        config.setName(proxy.getName());
        config.setLocalIp(proxy.getLocalIp());
        config.setLocalPort(proxy.getLocalPort());
        config.setRemotePort(proxy.getRemotePort());
        config.setProtocol(ProtocolType.getByName(proxy.getProtocol().name()));
        config.setProxyStatus(ProxyStatus.fromStatus(proxy.getStatus()));
        config.setAutoDomain(proxy.getAutoDomain());
        config.getCustomDomains().addAll(proxy.getCustomDomainsList());
        config.getSubDomains().addAll(proxy.getSubDomainsList());
        return config;
    }

    private Message.NewProxyResp buildResponse(ProxyConfig ext) {
        ProtocolType protocol = ext.getProtocol();
        Set<String> domains = ext.getFullDomains();
        Message.NewProxyResp.Builder builder = Message.NewProxyResp.newBuilder();
        if (domains == null || domains.isEmpty()) {
            return builder.build();
        }
        String host = ConfigHelper.get().getHost();
        StringBuilder remoteAddr = new StringBuilder();
        if (ProtocolType.isHttp(protocol)) {
            int httpProxyPort = ConfigHelper.get().getHttpProxyPort();
            for (String domain : domains) {
                remoteAddr.append("http://").append(domain);
                if (httpProxyPort != 80) {
                    remoteAddr.append(":").append(httpProxyPort);
                }
                remoteAddr.append("\n");
            }

        } else if (ProtocolType.isTcp(protocol)) {
            Integer remotePort = ext.getRemotePort();
            remoteAddr.append(host).append(":").append(remotePort);
        }
        builder.setProxyName(ext.getName());
        builder.setRemoteAddr(remoteAddr.toString());
        return builder.build();
    }
}
