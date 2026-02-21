package com.xiaoniucode.etp.client.config;

import com.xiaoniucode.etp.client.manager.AgentSessionManager;
import com.xiaoniucode.etp.core.domain.*;
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
        if (config.isOpen()) {
            builder.setStatus(config.getStatus().getCode());
        }
        if (config.isCompressEnabled()) {
            builder.setCompress(config.getCompress());
        }
        if (config.isEncryptEnabled()) {
            builder.setEncrypt(config.getEncrypt());
        }
        switch (protocol) {
            case TCP:
                if (config.hasRemotePort()) {
                    builder.setRemotePort(config.getRemotePort());
                }
                break;
            case HTTP:
                if (config.isAutoDomainEnabled()) {
                    builder.setAutoDomain(config.getAutoDomain());
                }
                builder.addAllCustomDomains(config.getCustomDomains());
                builder.addAllSubDomains(config.getSubDomains());
                //Basic Auth 认证
                if (config.hasBasicAuth()) {
                    BasicAuthConfig basicAuth = config.getBasicAuth();
                    Message.BasicAuth.Builder basicAuthBuilder = Message.BasicAuth.newBuilder().setEnable(basicAuth.isEnable());
                    Set<HttpUser> users = basicAuth.getUsers();
                    if (users != null && !users.isEmpty()) {
                        for (HttpUser user : users) {
                            Message.HttpUser httpUser = Message.HttpUser.newBuilder()
                                    .setUser(user.getUser())
                                    .setPass(user.getPass())
                                    .build();
                            basicAuthBuilder.addHttpUsers(httpUser);
                        }
                    }
                    builder.setBasicAuth(basicAuthBuilder.build());
                }
                break;
        }
        //访问控制
        if (config.hasAccessControl()) {
            AccessControlConfig access = config.getAccessControl();
            Message.AccessControl.Builder accessControlbuilder = Message.AccessControl
                    .newBuilder()
                    .setEnable(access.isEnable())
                    .setMode(Message.AccessMode.valueOf(access.getMode().name()));
            if (access.hasAllow()) {
                Set<String> allow = access.getAllow();
                accessControlbuilder.addAllAllow(allow);
            }
            if (access.hasDeny()) {
                Set<String> deny = access.getDeny();
                accessControlbuilder.addAllDeny(deny);
            }
            builder.setAccessControl(accessControlbuilder.build());
        }
        //带宽限制
        if (config.hasBandwidthLimit()) {
            BandwidthConfig bandwidth = config.getBandwidth();
            Message.Bandwidth bw = Message.Bandwidth.newBuilder()
                    .setLimit(bandwidth.getLimit())
                    .setLimitIn(bandwidth.getLimitIn())
                    .setLimitOut(bandwidth.getLimitOut())
                    .build();
            builder.setBandwidth(bw);
        }

        return builder.build();
    }
}
