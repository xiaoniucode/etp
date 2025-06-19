package cn.xilio.vine.client;

import cn.xilio.vine.core.ChannelUtils;
import io.netty.channel.Channel;

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

    /**
     * 关闭所有的真实服务连接通道同时清理掉所有缓存
     */
    public static void clearAllRealServerChannel() {
        Iterator<Map.Entry<Long, Channel>> iterator = realServerChannels.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Channel> entry = iterator.next();
            Channel channel = entry.getValue();
            ChannelUtils.closeOnFlush(channel);//如果是存活的则关闭
            iterator.remove();//清除缓存
        }
    }
}
