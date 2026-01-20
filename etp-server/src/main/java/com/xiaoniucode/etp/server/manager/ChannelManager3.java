package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.server.config.domain.AuthInfo;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道管理器
 *
 * @author liuxin
 */
public final class ChannelManager3 {
    private final static Logger logger = LoggerFactory.getLogger(ChannelManager3.class);
    /**
     * 客户端与控制通道映射
     */
    private static final Map<String, Channel> clientControlChannelMapping = new ConcurrentHashMap<>();
    /**
     * 公网服务端口与控制通道映射
     */
    private static final Map<Integer, Channel> portControlChannelMapping = new ConcurrentHashMap<>();
     /**
     * 只会在客户端认证的时候执行一次
     *
     * @param remotePorts    公网端口列表
     * @param secretKey      客户端密钥
     * @param controlChannel 控制隧道
     */
    public static void addControlChannel(List<Integer> remotePorts, String secretKey, String os, String arch,
        Channel controlChannel) {
        for (Integer remotePort : remotePorts) {
            portControlChannelMapping.put(remotePort, controlChannel);
        }
        controlChannel.attr(EtpConstants.CHANNEL_REMOTE_PORT).set(remotePorts);
        controlChannel.attr(EtpConstants.SECRET_KEY).set(secretKey);
        controlChannel.attr(EtpConstants.OS).set(os);
        controlChannel.attr(EtpConstants.ARCH).set(arch);
        controlChannel.attr(EtpConstants.VISITOR_CHANNELS).set(new ConcurrentHashMap<>());
        clientControlChannelMapping.put(secretKey, controlChannel);
    }





    public static void closeControlChannelByClient(String secretKey) {
        Channel channel = clientControlChannelMapping.get(secretKey);
        if (channel == null) {
            logger.warn("client channel is null");
            return;
        }
        clearControlChannel(channel);
    }

    public static void removeRemotePortToControlChannel(int remotePort) {
        portControlChannelMapping.remove(remotePort);
    }

    public static void clearControlChannel(Channel controlChannel) {
        if (controlChannel.attr(EtpConstants.CHANNEL_REMOTE_PORT).get() == null) {
            return;
        }
        String secretKey = controlChannel.attr(EtpConstants.SECRET_KEY).get();
        Channel existingChannel = clientControlChannelMapping.get(secretKey);
        if (controlChannel == existingChannel) {
            clientControlChannelMapping.remove(secretKey);
        }
        List<Integer> remotePorts = controlChannel.attr(EtpConstants.CHANNEL_REMOTE_PORT).get();
        if (remotePorts != null) {
            for (int port : remotePorts) {
                portControlChannelMapping.remove(port);
            }
        }
        if (controlChannel.isActive()) {
            controlChannel.close();
        }
        //将该隧道上所有用户连接都给关闭掉
        Map<Long, Channel> clientChannels =controlChannel.attr(EtpConstants.VISITOR_CHANNELS).get();
        if (clientChannels != null) {
            clientChannels.keySet().forEach(sessionId -> {
                Channel clientChannel = clientChannels.get(sessionId);
                if (clientChannel.isActive()) {
                    clientChannel.close();
                }
            });
        }
    }

    public static Channel removeClientChannelFromControlChannel(Channel controlChannel, Long sessionId) {
        if (controlChannel.attr(EtpConstants.VISITOR_CHANNELS).get() != null) {
            return controlChannel.attr(EtpConstants.VISITOR_CHANNELS).get().remove(sessionId);
        }
        return null;
    }

}
