package cn.xilio.vine.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
    /**
     * 保存一个访问者会话与内网真实服务通道的映射关系
     * <sessionId, channel>
     */
    private final static Map<Long, Channel> realServerChannels = new ConcurrentHashMap<>();
    /**
     * 当前控制隧道通道，每个客户端和远程代理服务器只有一条认证成功的控制隧道
     */
    private static Channel controlTunnelChannel;

    public static Channel getControlTunnelChannel() {
        return controlTunnelChannel;
    }

    public static void setControlTunnelChannel(Channel controlTunnelChannel) {
        ChannelManager.controlTunnelChannel = controlTunnelChannel;
    }

    public static void removeRealServerChannel(Long sessionId) {
        realServerChannels.remove(sessionId);
    }

    public static void addRealServerChannel(long sessionId, Channel realChannel) {
        realServerChannels.put(sessionId, realChannel);
    }

    public static void clearAllRealServerChannel() {
        Iterator<Map.Entry<Long, Channel>> iterator = realServerChannels.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Channel> entry = iterator.next();
            Channel channel = entry.getValue();
            if (channel.isActive()) {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                iterator.remove(); // 安全移除
            }
        }
    }
}
