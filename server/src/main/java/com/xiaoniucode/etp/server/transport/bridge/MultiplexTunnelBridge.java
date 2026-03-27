package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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
        int streamId = streamContext.getStreamId();
        if (!tunnel.isActive()) {
            logger.debug("数据通道未激活，数据转发失败：streamId={}", streamId);
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        TMSPFrame frame = new TMSPFrame(streamContext.getStreamId(), TMSP.MSG_STREAM_DATA, payload);
        logger.debug("[visitor-tunnel]流 {} 转发前引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
        tunnel.writeAndFlush(frame.retain()).addListener((ChannelFutureListener) future -> {
            logger.debug("[visitor-tunnel]流 {} 转发后引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
          //  ReferenceCountUtil.release(frame);
            if (!future.isSuccess()) {
                logger.debug("数据转发到内网失败，streamId={}", streamContext.getStreamId(), future.cause());
            } else {
                logger.debug("数据转发到内网成功，streamId={}", streamContext.getStreamId());
            }
        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        int streamId = streamContext.getStreamId();
        if (visitor == null || !visitor.isActive()) {
            logger.debug("通道未激活，数据转发失败：streamId={}", streamId);
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        visitor.writeAndFlush(payload.retain()).addListener((ChannelFutureListener) future -> {
            logger.debug("[tunnel-->visitor]流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
            if (!future.isSuccess()) {
                logger.debug("数据转发到访问者失败，streamId={}", streamId, future.cause());
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            } else {
                logger.debug("数据转发到访问者成功：streamId={}", streamId);
            }
        });
    }
}