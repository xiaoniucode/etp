package com.xiaoniucode.etp.server.handler.tunnel;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class ResourceReleaseHandler extends ChannelDuplexHandler {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("断开连接");
        super.channelInactive(ctx);
    }
}
