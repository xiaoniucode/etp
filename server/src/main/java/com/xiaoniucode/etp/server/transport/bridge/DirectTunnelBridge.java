package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.RawByteBufChannelHandler;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectTunnelBridge implements TunnelBridge {
    private final Logger logger = LoggerFactory.getLogger(DirectTunnelBridge.class);
    private final StreamContext streamContext;
    private final Channel visitor;
    private final Channel tunnel;

    public DirectTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
        this.visitor = streamContext.getVisitor();
        this.tunnel = streamContext.getTunnelEntry().getChannel();
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
        pipeline.addLast(new RawByteBufChannelHandler(streamContext, false));
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        if (!tunnel.isActive() || !tunnel.isWritable()) {
            logger.error("隧道不可写，丢弃数据：streamId={}", streamContext.getStreamId());
            ReferenceCountUtil.release(payload);
            return;
        }
        ReferenceCountUtil.retain(payload);
        tunnel.writeAndFlush(payload).addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                logger.debug("数据转发到内网成功：streamId={}", streamContext.getStreamId());
            } else {
                logger.debug("数据转发到内网失败：streamId={}", streamContext.getStreamId());
            }
            // ReferenceCountUtil.release(payload);
        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        if (!visitor.isActive() || !visitor.isWritable()) {
            logger.error("访问者通道不可写，丢弃数据：streamId={}", streamContext.getStreamId());
            ReferenceCountUtil.release(payload);
            return;
        }
        ReferenceCountUtil.retain(payload);
        visitor.writeAndFlush(payload).addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                logger.debug("数据转发给访问者成功：streamId={}", streamContext.getStreamId());
            } else {
                logger.debug("数据转发给访问者失败：streamId={}", streamContext.getStreamId());
            }
            // ReferenceCountUtil.release(payload);
        });
    }
}