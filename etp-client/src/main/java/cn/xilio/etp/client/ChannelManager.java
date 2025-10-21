package cn.xilio.etp.client;

import cn.xilio.etp.core.ChannelUtils;
import cn.xilio.etp.core.DataTunnelChannelBorrowCallback;
import cn.xilio.etp.core.EtpConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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
            dataTunnelChannel.attr(EtpConstants.NEXT_CHANNEL).remove();
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

    /**
     * 用于从数据隧道通道池中获取一个通道连接，如果没有则创建一个新的通道
     *
     * @param tunnelBootstrap 隧道
     * @param callback        回调接口
     */
    public static void borrowDataTunnelChanel(Bootstrap tunnelBootstrap, DataTunnelChannelBorrowCallback callback) {
        //从队列中压出一个通道
        Channel dataTunnelChannel = dataTunnelChannelPool.poll();
        if (dataTunnelChannel != null) {
            callback.success(dataTunnelChannel);
            return;
        }
        //如果连接池没有，则新建一个连接
        tunnelBootstrap.connect(Config.getInstance().getServerAddr(), Config.getInstance().getServerPort()).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                callback.success(future.channel());
            } else {
                callback.fail(future.cause());
            }
        });
    }
}
