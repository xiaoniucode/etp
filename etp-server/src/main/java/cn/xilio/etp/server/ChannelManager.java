package cn.xilio.etp.server;

import cn.xilio.etp.core.EtpConstants;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道管理器
 * @author xilio.cn
 */
public class ChannelManager {
    /**
     * 用于存储每个session连接对应的通道
     */
    private static final AttributeKey<Map<Long, Channel>> VISITOR_CHANNELS = AttributeKey.newInstance("user_channels");
    /**
     * 存储所有的客户端的控制通道
     */
    private static final Map<String, Channel> tunnelChannels = new ConcurrentHashMap<>();
    /**
     * 端口和隧道映射
     */
    private static final Map<Integer, Channel> portTunnelChannelMapping = new ConcurrentHashMap<>();
    private static final AttributeKey<List<Integer>> CHANNEL_PORT = AttributeKey.newInstance("channel_port");
    private static final AttributeKey<String> CHANNEL_SECRET_KEY = AttributeKey.newInstance("channel_client_key");

    /**
     * @param internalPorts 客户端内网服务的端口号
     * @param secretKey     客户端认证密钥
     * @param channel       安全认证后的隧道-通道
     */
    public static void addControlTunnelChannel(List<Integer> internalPorts, String secretKey, Channel channel) {
        //共享资源，put会在多个地方被调用，避免同时调用put导致线程安全问题
        synchronized (portTunnelChannelMapping) {
            for (Integer port : internalPorts) {
                portTunnelChannelMapping.put(port, channel);
            }
        }
        //该通道上的代理服务端口
        channel.attr(CHANNEL_PORT).set(internalPorts);
        //该条通道对应的客户端
        channel.attr(CHANNEL_SECRET_KEY).set(secretKey);
        channel.attr(VISITOR_CHANNELS).set(new ConcurrentHashMap<>());
        tunnelChannels.put(secretKey, channel);
    }

    public static Channel getControlTunnelChannel(int port) {
        return portTunnelChannelMapping.get(port);
    }

    public static Channel getControlTunnelChannel(String secretKey) {
        return tunnelChannels.get(secretKey);
    }

    public static Channel getVisitorChannel(Channel tunnelChannel, Long sessionId) {
        return tunnelChannel.attr(VISITOR_CHANNELS).get().get(sessionId);
    }

    public static void addVisitorChannelToTunnelChannel(Channel visitorChannel, Long sessionId, Channel turnnelChannel) {
        turnnelChannel.attr(VISITOR_CHANNELS).get().put(sessionId, visitorChannel);
        visitorChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
    }

    public static Channel removeVisitorChannelFromTunnelChannel(Channel tunnelChannel, Long sessionId) {
        if (tunnelChannel.attr(VISITOR_CHANNELS).get() == null) {
            return null;
        }
        return tunnelChannel.attr(VISITOR_CHANNELS).get().remove(sessionId);
    }

    public static void removeTunnelAndBindRelationship(Channel channel) {
        if (channel.attr(CHANNEL_PORT).get() == null) {
            return;
        }
        String clientKey = channel.attr(CHANNEL_SECRET_KEY).get();
        Channel channel0 = tunnelChannels.remove(clientKey);
        if (channel != channel0) {
            tunnelChannels.put(clientKey, channel);
        }

        List<Integer> ports = channel.attr(CHANNEL_PORT).get();
        for (int port : ports) {
            Channel visitorTunnelChannel = portTunnelChannelMapping.remove(port);
            if (visitorTunnelChannel == null) {
                continue;
            }
            if (visitorTunnelChannel != channel) {
                portTunnelChannelMapping.put(port, visitorTunnelChannel);
            }
        }

        if (channel.isActive()) {
            channel.close();
        }
        //将该隧道上所有用户连接都给关闭掉
        Map<Long, Channel> visitorChannels = getVisitorChannels(channel);
        visitorChannels.keySet().forEach(sessionId -> {
            Channel visitorChannel = visitorChannels.get(sessionId);
            if (visitorChannel.isActive()) {
                visitorChannel.close();
            }
        });

    }

    public static Map<Long, Channel> getVisitorChannels(Channel tunnelChannel) {
        return tunnelChannel.attr(VISITOR_CHANNELS).get();
    }

    public static Long getVisitorChannelSessionId(Channel visitorChannel) {
        return visitorChannel.attr(EtpConstants.SESSION_ID).get();
    }
}
