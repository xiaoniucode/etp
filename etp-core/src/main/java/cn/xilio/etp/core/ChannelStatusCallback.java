package cn.xilio.etp.core;

import io.netty.channel.ChannelHandlerContext;

public interface ChannelStatusCallback {

    void channelInactive(ChannelHandlerContext ctx);
}
