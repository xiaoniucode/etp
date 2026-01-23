package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ChannelManager {
    private volatile static Bootstrap realBootstrap;
    private volatile static Bootstrap controlBootstrap;
    private final static Map<Long, Channel> realServerChannels = new ConcurrentHashMap<>();
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
    public static void clearAllRealServerChannel() {
        Iterator<Map.Entry<Long, Channel>> iterator = realServerChannels.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Channel> entry = iterator.next();
            Channel channel = entry.getValue();
            ChannelUtils.closeOnFlush(channel);
            iterator.remove();
        }
    }
}
