package com.xiaoniucode.etp.server.transport.bridge;

import io.netty.channel.ChannelHandlerContext;

public abstract class ChannelBridgeDecorator extends BaseChannelBridge {
    protected final BaseChannelBridge delegate;

    public ChannelBridgeDecorator(ChannelBridge delegate) {
        super(
                ((BaseChannelBridge) delegate).streamContext,
                ((BaseChannelBridge) delegate).target,
                ((BaseChannelBridge) delegate).direction
        );
        this.delegate = (BaseChannelBridge) delegate;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!beforeChannelRead(ctx, msg)) {
            return;
        }
        delegate.channelRead(ctx, msg);
        afterChannelRead(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        delegate.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        delegate.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        delegate.channelWritabilityChanged(ctx);
    }

    protected boolean beforeChannelRead(ChannelHandlerContext ctx, Object msg) {
        return true;
    }

    protected void afterChannelRead(ChannelHandlerContext ctx, Object msg) {
    }
}