package cn.xilio.vine.core;

import io.netty.channel.ChannelHandlerContext;

public interface ChannelStatusCallback {

    void channelInactive(ChannelHandlerContext ctx);

}
