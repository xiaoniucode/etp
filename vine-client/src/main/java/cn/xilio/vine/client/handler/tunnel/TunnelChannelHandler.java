package cn.xilio.vine.client.handler.tunnel;

import cn.xilio.vine.core.Constants;
import cn.xilio.vine.core.MessageHandler;
import cn.xilio.vine.core.protocol.TunnelMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

public class TunnelChannelHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {
    private final Bootstrap realBootstrap;
    private final Bootstrap tunnelBootstrap;

    public TunnelChannelHandler(Bootstrap realBootstrap, Bootstrap tunnelBootstrap) {
        this.realBootstrap = realBootstrap;
        this.tunnelBootstrap = tunnelBootstrap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        MessageHandler handler = MessageHandlerFactory.getHandler(msg.getType());
        ctx.channel().attr(Constants.REAL_BOOTSTRAP).set(realBootstrap);
        ctx.channel().attr(Constants.TUNNEL_BOOTSTRAP).set(tunnelBootstrap);
        handler.handle(ctx, msg);
    }
}
