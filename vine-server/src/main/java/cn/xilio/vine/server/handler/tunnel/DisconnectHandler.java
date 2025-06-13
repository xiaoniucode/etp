package cn.xilio.vine.server.handler.tunnel;

import cn.xilio.vine.core.AbstractMessageHandler;
import cn.xilio.vine.core.protocol.TunnelMessage;
import io.netty.channel.ChannelHandlerContext;

public class DisconnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {

    }
}
