package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplexTunnelBridge implements TunnelBridge {
    private final Logger logger = LoggerFactory.getLogger(MultiplexTunnelBridge.class);
    private final StreamContext streamContext;
    private final TunnelEntry tunnelEntry;
    private final Channel visitor;

    public MultiplexTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
        this.tunnelEntry = streamContext.getTunnelEntry();
        this.visitor = streamContext.getVisitor();
    }

    @Override
    public void open() {
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        Channel tunnel = tunnelEntry.getChannel();
        if (tunnel == null) {
            ReferenceCountUtil.release(payload);
            return;
        }
        TMSPFrame frame = new TMSPFrame(streamContext.getStreamId(), TMSP.MSG_STREAM_DATA, payload.retain());
        tunnel.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
            ReferenceCountUtil.release(payload);
            if (!future.isSuccess()) {
                logger.debug("数据转发到内网失败，streamId={}", streamContext.getStreamId());
            } else {
                logger.debug("数据转发到内网成功，streamId={}", streamContext.getStreamId());
            }
        });

    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        if (payload == null) {
            return;
        }

        Channel visitor = streamContext.getVisitor();
        int streamId = streamContext.getStreamId();
        Target currentTarget = streamContext.getCurrentTarget();

        if (visitor == null || !visitor.isActive()) {
            logger.debug("通道未激活，数据转发失败：streamId={}", streamId);
            payload.release();
            return;
        }
        visitor.writeAndFlush(payload.retain()).addListener((ChannelFutureListener) future -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
            ReferenceCountUtil.release(payload);
            if (!future.isSuccess()) {
                visitor.close();
                logger.debug("数据转发到访问者失败，streamId={}", streamId);
            } else {
                logger.debug("数据转发到访问者成功：streamId={}", streamId);
            }
        });
    }
}