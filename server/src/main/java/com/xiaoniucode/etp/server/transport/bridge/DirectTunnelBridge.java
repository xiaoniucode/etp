package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;

public class DirectTunnelBridge implements TunnelBridge {
    private final StreamContext streamContext;
    private final Channel visitor;
    private final Channel tunnel;

    public DirectTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
        this.visitor = streamContext.getVisitor();
        this.tunnel = streamContext.getTunnel();
    }

    @Override
    public void open() {
        ChannelPipeline pipeline = tunnel.pipeline();
        String[] handlersToRemove = {
                NettyConstants.TMSP_CODEC,
                NettyConstants.CONTROL_FRAME_HANDLER
        };
        for (String handlerName : handlersToRemove) {
            if (pipeline.get(handlerName) != null) {
                pipeline.remove(handlerName);
            }
        }
    }

    @Override
    public void relayToTunnel(ByteBuf payload) {
        if (!tunnel.isActive() || !tunnel.isWritable()) {
            ReferenceCountUtil.release(payload);
            return;
        }
        ReferenceCountUtil.retain(payload);
        tunnel.writeAndFlush(payload).addListener((ChannelFutureListener) f -> {
            ReferenceCountUtil.release(payload);
        });
    }

    @Override
    public void relayToVisitor(ByteBuf payload) {
        if (!visitor.isActive() || !visitor.isWritable()) {
            ReferenceCountUtil.release(payload);
            return;
        }
        ReferenceCountUtil.retain(payload);
        visitor.writeAndFlush(payload).addListener((ChannelFutureListener) f -> {
            ReferenceCountUtil.release(payload);
        });
    }
}