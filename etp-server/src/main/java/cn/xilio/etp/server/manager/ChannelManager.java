package cn.xilio.etp.server.manager;

import cn.xilio.etp.core.EtpConstants;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通道管理器
 *
 * @author liuxin
 */
public final class ChannelManager {
    private static Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    /**
     * 客户端与控制通道映射
     */
    private static final Map<String, Channel> clientControlChannelMapping = new ConcurrentHashMap<>();
    /**
     * 公网服务端口与控制通道映射
     */
    private static final Map<Integer, Channel> portControlChannelMapping = new ConcurrentHashMap<>();
    /**
     * 用于记录每个公网端口下有哪些活跃的连接channel
     */
    private static Map<Integer, Set<Channel>> activeChannels = new ConcurrentHashMap<>();

    private final static Lock LOCK = new ReentrantLock();

    /**
     * 只会在客户端认证的时候执行一次
     *
     * @param remotePorts    公网端口列表
     * @param secretKey      客户端密钥
     * @param controlChannel 控制隧道
     */
    public static void addControlChannel(List<Integer> remotePorts, String secretKey, Channel controlChannel) {
        for (Integer remotePort : remotePorts) {
            portControlChannelMapping.put(remotePort, controlChannel);
        }
        controlChannel.attr(EtpConstants.CHANNEL_PORT).set(remotePorts);
        controlChannel.attr(EtpConstants.SECRET_KEY).set(secretKey);
        controlChannel.attr(EtpConstants.CLIENT_CHANNELS).set(new ConcurrentHashMap<>());
        clientControlChannelMapping.put(secretKey, controlChannel);
    }

    /**
     * 如果客户端已经启动并认证成功，将新增加的远程端口附加到控制隧道。如果没有启动，客户端启动的时候会执行addControlChannel添加所有端口。
     *
     * @param secretKey  客户端密钥
     * @param remotePort 需要新添加的端口
     */
    public static void addPortToControlChannelIfOnline(String secretKey, Integer remotePort) {
        LOCK.lock();
        try {
            Channel controlChannel = clientControlChannelMapping.get(secretKey);
            if (null == controlChannel) {
                return;
            }
            portControlChannelMapping.put(remotePort, controlChannel);
            controlChannel.attr(EtpConstants.CHANNEL_PORT).get().add(remotePort);
        } finally {
            LOCK.unlock();
        }
    }

    public static boolean clientIsOnline(String secretKey) {
        return clientControlChannelMapping.get(secretKey) != null;
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
        if (controlChannel.attr(EtpConstants.CHANNEL_PORT).get() == null) {
            return;
        }
        LOCK.lock();
        try {
            String secretKey = controlChannel.attr(EtpConstants.SECRET_KEY).get();
            Channel existingChannel = clientControlChannelMapping.get(secretKey);
            if (controlChannel == existingChannel) {
                clientControlChannelMapping.remove(secretKey);
            }
            List<Integer> remotePorts = controlChannel.attr(EtpConstants.CHANNEL_PORT).get();
            if (remotePorts != null) {
                for (int port : remotePorts) {
                    portControlChannelMapping.remove(port);
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

    public static Channel getControlChannelByPort(int remotePort) {
        return portControlChannelMapping.get(remotePort);
    }

    public static Channel getControlChannelBySecretKey(String secretKey) {
        return clientControlChannelMapping.get(secretKey);
    }

    public static Channel getClientChannel(Channel controlChannel, Long sessionId) {
        return controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get().get(sessionId);
    }

    public static void addClientChannelToControlChannel(Channel clientChannel, Long sessionId, Channel controlChannel) {
        controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get().put(sessionId, clientChannel);
        clientChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
    }

    public static void registerActiveConnection(int remotePort, Channel channel) {
        activeChannels.computeIfAbsent(remotePort, k -> ConcurrentHashMap.newKeySet()).add(channel);
    }

    public static void unregisterActiveConnection(int remotePort, Channel channel) {
        Set<Channel> set = activeChannels.get(remotePort);
        if (set != null) {
            set.remove(channel);
            if (set.isEmpty()) {
                activeChannels.remove(remotePort);
            }
        }
    }

    public static Channel removeClientChannelFromControlChannel(Channel controlChannel, Long sessionId) {
        if (controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get() != null) {
            return controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get().remove(sessionId);
        }
        return null;
    }

    public static Map<Long, Channel> getClientChannelsByControlChannel(Channel controlChannel) {
        return controlChannel.attr(EtpConstants.CLIENT_CHANNELS).get();
    }

    public static Long getSessionIdByClientChannel(Channel clientChannel) {
        return clientChannel.attr(EtpConstants.SESSION_ID).get();
    }

    /**
     * 获取在线客户端数量
     */
    public static int onlineClientCount() {
        return clientControlChannelMapping.size();
    }

    /**
     * 获取公网端口所有的用户连接
     *
     * @param remotePort 公网端口
     * @return 所有活跃的连接
     */
    public static Set<Channel> getActiveChannelsByRemotePort(Integer remotePort) {
        return activeChannels.get(remotePort);
    }

    /**
     * 将公网端口用户连接完全删除
     *
     * @param remotePort 公网端口
     */
    public static void removeActiveChannels(Integer remotePort) {
        activeChannels.remove(remotePort);
    }
}
