package cn.xilio.vine.console;

import io.netty.channel.Channel;

public final class ChannelHelper {
    private static Channel channel;

    public static Channel get() {
        return ChannelHelper.channel;
    }

    public static void set(Channel channel) {
        ChannelHelper.channel = channel;
    }
}
