package com.xiaoniucode.etp.client.transport.bridge;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.core.transport.compress.CompressionType;
import com.xiaoniucode.etp.core.transport.compress.Compressor;
import com.xiaoniucode.etp.core.transport.compress.CompressorFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MultiplexTunnelBridge implements TunnelBridge {
    private final Logger logger = LoggerFactory.getLogger(MultiplexTunnelBridge.class);
    private final StreamContext streamContext;
    private final Channel tunnel;
    private final Channel server;

    public MultiplexTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
        this.tunnel = streamContext.getTunnelEntry().getChannel();
        this.server = streamContext.getServer();
    }

    @Override
    public void open() {
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        int streamId = streamContext.getStreamId();
        if (server == null || !server.isActive()) {
            logger.debug("通道未激活，数据转发到内网失败，关闭流：streamId={}", streamId);
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        server.writeAndFlush(payload.retain()).addListener((ChannelFutureListener) future -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
          //  ReferenceCountUtil.release(payload);
            if (!future.isSuccess()) {
                logger.warn("数据转发到内网真实服务失败，关闭流：streamId={}", streamId, future.cause());
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            } else {
                logger.debug("数据转发到内网真实服务成功：streamId={}", streamId);
            }
        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        if (tunnel == null || !tunnel.isActive()) {
            logger.debug("通道未激活，数据转发到远程失败，关闭流：streamId={}", streamContext.getStreamId());
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        TMSPFrame frame = new TMSPFrame(streamContext.getStreamId(), TMSP.MSG_STREAM_DATA, payload);
        tunnel.writeAndFlush(frame.retain()).addListener((ChannelFutureListener) future -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
           // ReferenceCountUtil.release(payload);
            int streamId = streamContext.getStreamId();
            if (!future.isSuccess()) {
                logger.warn("数据转发到远程隧道失败: streamId={}", streamId, future.cause());
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            } else {
                logger.debug("数据转发到远程隧道成功: streamId={}", streamId);
            }
        });
    }
}