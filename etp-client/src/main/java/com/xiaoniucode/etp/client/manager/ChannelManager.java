package com.xiaoniucode.etp.client.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

public final class ChannelManager {
    private volatile static Bootstrap realBootstrap;
    private volatile static Bootstrap controlBootstrap;
    private static Channel controlChannel;

    public static void initBootstraps(Bootstrap control, Bootstrap real) {
        controlBootstrap = control;
        realBootstrap = real;
    }

    public static Bootstrap getRealServerBootstrap() {
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
}
