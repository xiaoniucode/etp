package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplexTunnelBridge implements TunnelBridge {
    private final Logger logger = LoggerFactory.getLogger(MultiplexTunnelBridge.class);
    private final StreamContext streamContext;

    public MultiplexTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
    }

    @Override
    public void open() {
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        if (!streamContext.isMultiplex()) {
            payload.release();
            return;
        }
        Channel tunnel = streamContext.getTunnel();
        if (tunnel == null) {
            payload.release();
            return;
        }
        ReferenceCountUtil.retain(payload);
        TMSPFrame frame = new TMSPFrame(streamContext.getStreamId(), TMSP.MSG_STREAM_DATA, payload);
        tunnel.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {

        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        Channel visitor = streamContext.getVisitor();
        int streamId = streamContext.getStreamId();
        Target currentTarget = streamContext.getCurrentTarget();
        if (payload == null || payload.refCnt() <= 0) {
            return;
        }
        if (visitor == null || !visitor.isActive()) {
            logger.debug("通道未激活，数据转发失败：streamId={}", streamId);
            payload.release();
            return;
        }
        ReferenceCountUtil.retain(payload);
        visitor.writeAndFlush(payload).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                visitor.close();
                logger.debug("数据转发失败，目标服务：host={},port={}", currentTarget.getHost(), currentTarget.getPort());
            } else {
                logger.debug("数据转发到隧道成功：streamId={}", streamId);
            }
        });
    }
}