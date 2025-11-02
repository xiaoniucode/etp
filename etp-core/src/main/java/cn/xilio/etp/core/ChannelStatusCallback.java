package cn.xilio.etp.core;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author liuxin
 */
public interface ChannelStatusCallback {

    void channelInactive(ChannelHandlerContext ctx);
}
