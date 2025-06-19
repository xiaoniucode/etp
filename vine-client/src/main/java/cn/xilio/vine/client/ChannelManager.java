package cn.xilio.vine.client;

import cn.xilio.vine.core.ChannelUtils;
import cn.xilio.vine.core.VineConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    /**
     * 最大数据隧道通道池大小，超过该大小则关闭当前的通道丢弃
     */
    private static final int MAX_DATA_TUNNEL_CHANNEL_POOL_SIZE = 100;
    /**
     *
     */
    private static final Queue<Channel> dataTunnelChannelPool = new ConcurrentLinkedQueue<>();

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

    /**
     * 归还数据隧道通道
     * 如果数据隧道通道池已满则关闭该通道
     *
     * @param dataTunnelChannel 数据隧道通道
     */
    public static void returnDataTunnelChanel(Channel dataTunnelChannel) {
        if (dataTunnelChannelPool.size() > MAX_DATA_TUNNEL_CHANNEL_POOL_SIZE) {
            dataTunnelChannel.close();
        } else {
            dataTunnelChannel.config().setOption(ChannelOption.AUTO_READ, true);
            dataTunnelChannel.attr(VineConstants.NEXT_CHANNEL).remove();
            dataTunnelChannelPool.offer(dataTunnelChannel);
        }
    }

    /**
     * 删除数据隧道通道
     *
     * @param dataTunnelChannel 数据隧道通道
     */
    public static void removeDataTunnelChanel(Channel dataTunnelChannel) {
        dataTunnelChannelPool.remove(dataTunnelChannel);
    }
}
