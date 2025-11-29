package cn.xilio.etp.server;

import cn.xilio.etp.core.EtpConstants;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通道管理器
 *
 * @author liuxin
 */
public class ChannelManager {
    /**
     * 内网代理客户端与控制通道映射
     */
    private static final Map<String, Channel> CONTROL_CHANNELS = new ConcurrentHashMap<>(4);
    /**
     * 内网服务端口与控制通道映射
     */
    private static final Map<Integer, Channel> PORT_CONTROL_CHANNEL_MAPPING = new ConcurrentHashMap<>(64);
    private final static Lock LOCK = new ReentrantLock();

    public static void addControlChannel(List<Integer> remotePorts, String secretKey, Channel controlChannel) {
        LOCK.lock();
        try {
            for (Integer port : remotePorts) {
                PORT_CONTROL_CHANNEL_MAPPING.put(port, controlChannel);
            }
            controlChannel.attr(EtpConstants.CHANNEL_PORT).set(remotePorts);
            controlChannel.attr(EtpConstants.SECRET_KEY).set(secretKey);
            controlChannel.attr(EtpConstants.CLIENT_CHANNELS).set(new ConcurrentHashMap<>());
            CONTROL_CHANNELS.put(secretKey, controlChannel);
        } finally {
            LOCK.unlock();
        }
    }

    public static void closeControlChannelByClient(String secretKey) {
        Channel channel = CONTROL_CHANNELS.get(secretKey);
        clearControlChannel(channel);
    }

    public static void removeRemotePortToControlChannel(String secretKey, int remotePort) {
        CONTROL_CHANNELS.remove(secretKey);
        PORT_CONTROL_CHANNEL_MAPPING.remove(remotePort);
    }

    public static void clearControlChannel(Channel controlChannel) {
        if (controlChannel.attr(EtpConstants.CHANNEL_PORT).get() == null) {
            return;
        }
        LOCK.lock();
        try {
            String secretKey = controlChannel.attr(EtpConstants.SECRET_KEY).get();
            Channel existingChannel = CONTROL_CHANNELS.get(secretKey);
            if (controlChannel == existingChannel) {
                CONTROL_CHANNELS.remove(secretKey);
            }
            List<Integer> remotePorts = controlChannel.attr(EtpConstants.CHANNEL_PORT).get();
            if (remotePorts != null) {
                for (int port : remotePorts) {
                    PORT_CONTROL_CHANNEL_MAPPING.remove(port);
                }
            }
        } finally {
            LOCK.unlock();
        }
        if (controlChannel.isActive()) {
            controlChannel.close();
        }
        //将该隧道上所有用户连接都给关闭掉
        Map<Long, Channel> clientChannels = getClientChannelsByControlChannel(controlChannel);
        if (clientChannels != null) {
            clientChannels.keySet().forEach(sessionId -> {
                Channel clientChannel = clientChannels.get(sessionId);
                if (clientChannel.isActive()) {
                    clientChannel.close();
                }
            });
        }
    }

    public static Channel getControlChannelByPort(int port) {
        return PORT_CONTROL_CHANNEL_MAPPING.get(port);
    }

    public static Channel getControlChannelBySecretKey(String secretKey) {
        return CONTROL_CHANNELS.get(secretKey);
    }

    public static Channel getClientChannel(Channel controlChannel, Long sessionId) {
        return controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get().get(sessionId);
    }

    public static void addClientChannelToControlChannel(Channel clientChannel, Long sessionId, Channel controlChannel) {
        controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get().put(sessionId, clientChannel);
        clientChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
    }

    public static Channel removeClientChannelFromControlChannel(Channel controlChannel, Long sessionId) {
        if (controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get() == null) {
            return null;
        }
        return controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get().remove(sessionId);
    }

    public static Map<Long, Channel> getClientChannelsByControlChannel(Channel controlChannel) {
        return controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get();
    }

    public static Long getSessionIdByClientChannel(Channel clientChannel) {
        return clientChannel.attr(EtpConstants.SESSION_ID).get();
    }
}
