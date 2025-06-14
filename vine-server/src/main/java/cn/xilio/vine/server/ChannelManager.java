package cn.xilio.vine.server;

import cn.xilio.vine.core.VineConstants;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道管理器
 */
public class ChannelManager {
    private static final AttributeKey<Map<Long, Channel>> VISITOR_CHANNELS = AttributeKey.newInstance("user_channels");
    private static final Map<String, Channel> tunnelChannel = new ConcurrentHashMap<>();
    private static final Map<Integer, Channel> portTunnelChannelMapping = new ConcurrentHashMap<>();

    /**
     *
     * @param internalPorts 客户端内网服务的端口号
     * @param secretKey 客户端认证密钥
     * @param channel 安全认证后的隧道-通道
     */
    public static void addTunnelChannel(List<Integer> internalPorts, String secretKey, Channel channel) {
        for (Integer port : internalPorts) {
            portTunnelChannelMapping.put(port, channel);
        }
        channel.attr(VISITOR_CHANNELS).set(new ConcurrentHashMap<>());
        tunnelChannel.put(secretKey, channel);
    }

    public static Channel getTunnelChannel(int port) {
        return portTunnelChannelMapping.get(port);
    }

    public static Channel getTunnelChannel(String secretKey) {
        return tunnelChannel.get(secretKey);
    }

    public static Channel getVisitorChannel(Channel tunnelChannel, Long sessionId) {
        return tunnelChannel.attr(VISITOR_CHANNELS).get().get(sessionId);
    }

    public static void addVisitorChannelToTunnelChannel(Channel visitorChannel, Long sessionId, Channel turnnelChannel) {
        turnnelChannel.attr(VISITOR_CHANNELS).get().put(sessionId, visitorChannel);
        visitorChannel.attr(VineConstants.SESSION_ID).set(sessionId);
    }
}
