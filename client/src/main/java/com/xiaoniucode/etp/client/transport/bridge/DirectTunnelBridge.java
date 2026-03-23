package com.xiaoniucode.etp.client.transport.bridge;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.RawByteBufChannelHandler;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
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
    private final Channel tunnel;
    private final Channel server;

    public DirectTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
        this.tunnel = streamContext.getTunnelEntry().getChannel();
        this.server = streamContext.getServer();
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
        pipeline.addLast(new RawByteBufChannelHandler(streamContext, true));
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        if (!server.isActive() || !server.isWritable()) {
            logger.error("服务器连接不可写，丢弃数据：streamId={}", streamContext.getStreamId());
            ReferenceCountUtil.release(payload);
            return;
        }
        ReferenceCountUtil.retain(payload);
        server.writeAndFlush(payload).addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                logger.debug("数据成功转发给服务");
            } else {
                logger.debug("数据转发给服务失败");
            }
           // ReferenceCountUtil.release(payload);
        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        if (!tunnel.isActive() || !tunnel.isWritable()) {
            logger.error("隧道不可写，丢弃数据：streamId={}", streamContext.getStreamId());
            ReferenceCountUtil.release(payload);
            return;
        }
        ReferenceCountUtil.retain(payload);
        tunnel.writeAndFlush(payload).addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                logger.debug("数据成功转发到远程");
            } else {
                logger.debug("数据转发到远程失败");
            }
           // ReferenceCountUtil.release(payload);
        });
    }
}