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
}
