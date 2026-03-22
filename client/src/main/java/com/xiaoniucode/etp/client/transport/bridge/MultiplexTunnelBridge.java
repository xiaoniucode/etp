package com.xiaoniucode.etp.client.transport.bridge;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
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
        Channel server = streamContext.getServer();
        int streamId = streamContext.getStreamId();
        if (server == null || !server.isActive()) {
            logger.debug("通道未激活，数据转发到内网失败：streamId={}", streamId);
            ReferenceCountUtil.release(payload);
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        ReferenceCountUtil.retain(payload);
        server.writeAndFlush(payload).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                server.close();
                logger.debug("数据转发到内网失败");
            } else {
                logger.debug("数据转发到内网成功：streamId={}", streamId);
            }
           // ReferenceCountUtil.safeRelease(payload);
        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
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
            if (!future.isSuccess()) {
                logger.debug("数据转发给访问者失败");
            } else {
                logger.debug("数据转发给访问者成功");
            }
        });
    }
}