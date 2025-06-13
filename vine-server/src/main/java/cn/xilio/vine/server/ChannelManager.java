package cn.xilio.vine.server;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
    private static final AttributeKey<Map<String, Channel>> VISITOR_CHANNELS = AttributeKey.newInstance("user_channels");
    private static Map<String, Channel> tunnelChannel = new ConcurrentHashMap<>();
    private static Map<Integer, Channel> portTunnelChannelMapping = new ConcurrentHashMap<>();

    public static void addTunnelChannel(int port, String authToken, Channel channel) {
        portTunnelChannelMapping.put(port, channel);
        channel.attr(VISITOR_CHANNELS).set(new ConcurrentHashMap<>());
        tunnelChannel.put(authToken, channel);
    }

    public static Channel getTunnelChannel(int port) {
        return portTunnelChannelMapping.get(port);
    }

    public static Channel getTunnelChannel(String authToken) {
        return tunnelChannel.get(authToken);
    }

    public static Channel getVisitorChannel(Channel tunnelChannel, String visitorId) {
        return tunnelChannel.attr(VISITOR_CHANNELS).get().get(visitorId);
    }

    public static void addVisitorChannelToTunnelChannel(Channel visitorChannel, String visitorId, Channel turnnelChannel) {
        turnnelChannel.attr(VISITOR_CHANNELS).get().put(visitorId, visitorChannel);
    }
}
