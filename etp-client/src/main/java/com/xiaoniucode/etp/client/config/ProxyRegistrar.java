package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.manager.AgentSessionManager;
import com.xiaoniucode.etp.core.domain.AccessControlConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
                .setLocalPort(config.getLocalPort())
                .setLocalIp(config.getLocalIp())
                .setProtocol(Message.ProtocolType.valueOf(config.getProtocol().name()));
        if (config.getStatus() != null) {
            builder.setStatus(config.getStatus().getCode());
        }
        if (config.getCompress() != null) {
            builder.setCompress(config.getCompress());
        }
        if (config.getEncrypt() != null) {
            builder.setEncrypt(config.getEncrypt());
        }
        switch (protocol) {
            case TCP:
                Integer remotePort = config.getRemotePort();
                if (remotePort != null) {
                    builder.setRemotePort(remotePort);
                }
                break;
            case HTTP:
                if (config.getAutoDomain() != null) {
                    builder.setAutoDomain(config.getAutoDomain());
                }
                builder.addAllCustomDomains(config.getCustomDomains());
                builder.addAllSubDomains(config.getSubDomains());
                break;
        }
        AccessControlConfig access = config.getAccessControl();
        if (access != null) {
            Message.AccessControl.Builder accessControlbuilder = Message.AccessControl
                    .newBuilder()
                    .setEnable(access.isEnable())
                    .setMode(Message.AccessMode.valueOf(access.getMode().name()));
            Set<String> allow = access.getAllow();
            Set<String> deny = access.getDeny();
            if (allow != null && !allow.isEmpty()) {
                accessControlbuilder.addAllAllow(allow);
            }
            if (deny != null && !deny.isEmpty()) {
                accessControlbuilder.addAllDeny(deny);
            }
            builder.setAccessControl(accessControlbuilder.build());
        }
        return builder.build();
    }
}
