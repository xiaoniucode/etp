package cn.xilio.etp.core;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 通道工具类
 */
public final class ChannelUtils {

    /**
     * 如果channel是活跃的，那么关闭它
     *
     * @param ch 通道
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private ChannelUtils() {
    }
}
