package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.manager.AgentSessionManager;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * 用于将代理配置发送到代理服务器
 */
public class ProxyRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(ProxyRegistrar.class);

    /**
     * 发送单个配置信息
     *
     * @param config 配置信息
     */
    public static void register(ProxyConfig config) {
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.NEW_PROXY)
                .build();

        Message.NewProxy newProxy = buildNewProxy(config);
        Message.ControlMessage message = Message.ControlMessage.newBuilder().setHeader(header).setNewProxy(newProxy).build();

        AgentSessionManager.getControl().ifPresent(control -> {
            control.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.debug("代理配置信息发送成功");
                }
            });
        });
    }

    /**
     * 批量发送
     *
     */
    public static void registerBatch(List<ProxyConfig> proxies) {
        Optional<Channel> optional = AgentSessionManager.getControl();
        if (optional.isEmpty()) {
            return;
        }
        Channel control = optional.get();
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.NEW_PROXY)
                .build();
        for (ProxyConfig config : proxies) {
            Message.NewProxy newProxy = buildNewProxy(config);
            Message.ControlMessage message = Message.ControlMessage.newBuilder().setHeader(header).setNewProxy(newProxy).build();
            //添加到缓冲区
            control.write(message);
        }
        //一次性发送到代理服务器
        control.flush();
    }

    private static Message.NewProxy buildNewProxy(ProxyConfig config) {
        ProtocolType protocol = config.getProtocol();
        Message.NewProxy.Builder builder = Message.NewProxy.newBuilder();
        builder.setName(config.getName())
                .setLocalIp(config.getLocalIp())
                .setLocalPort(config.getLocalPort())
                .setStatus(config.getStatus().getStatus())
                .setCompress(config.getCompress())
                .setEncrypt(config.getEncrypt())
                .setProtocol(Message.ProtocolType.valueOf(config.getProtocol().name()));
        switch (protocol) {
            case TCP:
                Integer remotePort = config.getRemotePort();
                if (remotePort != null) {
                    builder.setRemotePort(remotePort);
                }
                break;
            case HTTP:
                builder.setAutoDomain(config.getAutoDomain());
                builder.addAllCustomDomains(config.getCustomDomains());
                builder.addAllSubDomains(config.getSubDomains());
                break;
        }
        return builder.build();
    }
}
