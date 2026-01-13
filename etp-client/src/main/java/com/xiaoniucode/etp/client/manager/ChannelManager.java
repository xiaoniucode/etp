package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 连接池管理、channel管理、Bootstrap管理
 *
 * @author liuxin
 */
public final class ChannelManager {
    private volatile static Bootstrap realBootstrap;
    private volatile static Bootstrap controlBootstrap;
    /**
     * 保存一个访问者会话与内网真实服务通道的映射关系
     * <sessionId, channel>
     */
    private final static Map<Long, Channel> realServerChannels = new ConcurrentHashMap<>();
    /**
     * 当前控制隧道通道，每个客户端和远程代理服务器只有一条认证成功的控制隧道
     */
    private static Channel controlChannel;
    /**
     * 最大数据隧道通道池大小，超过该大小则关闭当前的通道丢弃
     */
    private static final int MAX_DATA_TUNNEL_CHANNEL_POOL_SIZE = 1000;
    /**
     *
     */
    private static final Queue<Channel> dataTunnelChannelPool = new ConcurrentLinkedQueue<>();

    public static void initBootstraps(Bootstrap control, Bootstrap real) {
        controlBootstrap = control;
        realBootstrap = real;
    }

    public static Bootstrap getRealBootstrap() {
        return realBootstrap;
    }

    public static Bootstrap getControlBootstrap() {
        return controlBootstrap;
    }

    public static Channel getControlChannel() {
        return controlChannel;
    }

    public static void setControlChannel(Channel controlChannel) {
        ChannelManager.controlChannel = controlChannel;
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
            ChannelUtils.closeOnFlush(channel);
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
            dataTunnelChannel.attr(EtpConstants.REAL_SERVER_CHANNEL).getAndSet(null);
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
     * 获取数据隧道，没有则新建一个
     *
     * @param tunnelBootstrap 隧道
     */
    public static CompletableFuture<Channel> borrowDataTunnelChannel(Bootstrap tunnelBootstrap) {
        CompletableFuture<Channel> future = new CompletableFuture<>();
        Channel dataTunnelChannel = dataTunnelChannelPool.poll();
        if (dataTunnelChannel != null) {
            future.complete(dataTunnelChannel);
            return future;
        }

        Channel controlChannel = getControlChannel();
        String serverAddr = controlChannel.attr(EtpConstants.SERVER_DDR).get();
        Integer serverPort = controlChannel.attr(EtpConstants.SERVER_PORT).get();
        tunnelBootstrap.connect(serverAddr, serverPort).addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                future.complete(f.channel());
            } else {
                future.completeExceptionally(f.cause());
            }
        });

        return future;
    }

}
