package com.xiaoniucode.etp.server.transport.http;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ChannelHandler.Sharable
public class HttpVisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(HttpVisitorHandler.class);
    @Autowired
    private StreamManager streamManager;
    @Autowired
    @Qualifier("streamStateMachine")
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel visitor = ctx.channel();
        Optional<StreamContext> contextOpt = streamManager.getStreamContext(visitor);
        if (contextOpt.isPresent()) {
            StreamContext streamContext = contextOpt.get();
            if (streamContext.getState() == StreamState.OPENED) {
                TunnelEntry tunnelEntry = streamContext.getTunnelEntry();
                Channel tunnel = tunnelEntry.getChannel();
                if (!tunnel.isWritable()) {
                    logger.warn("数据无法转发到内网，流量过高，隧道不可写，暂停访问者读取");
                    visitor.config().setOption(ChannelOption.AUTO_READ, false);
                    streamManager.addPausedStreamId(tunnel, streamContext.getStreamId());
                }
                streamContext.forwardToLocal(buf);
            } else {
                logger.error("隧道未开启，无法传输数据");
            }
        } else {
            logger.debug("[HTTP] 创建流上下文");
            buf.retain();
            visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).set(buf);
            StreamContext streamContext = streamManager.createStreamContext(visitor, stateMachine);
            streamContext.setCurrentProtocol(ProtocolType.HTTP);
            streamContext.setStreamManager(streamManager);
            streamContext.fireEvent(StreamEvent.STREAM_OPEN);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel visitor = ctx.channel();
        streamManager.getStreamContext(visitor).ifPresent(streamContext -> {
            logger.debug("访问者连接断开，关闭流: streamId={}", streamContext.getStreamId());
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        streamManager.getStreamContext(ctx.channel()).ifPresent(streamContext -> {
            logger.warn("[HTTP] 访问者连接发生异常，关闭流: streamId={}", streamContext.getStreamId());
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
        });
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        logger.warn("[HTTP] 访问者可写性发生变化：{}", visitor.isWritable());
        streamManager.getStreamContext(visitor).ifPresent(streamContext -> {
            Channel tunnel = streamContext.getTunnelEntry().getChannel();
            if (tunnel != null) {
                logger.warn("流量过高，触发背压");
                boolean writable = visitor.isWritable();
                tunnel.config().setOption(ChannelOption.AUTO_READ, writable);
                if (writable) {
                    tunnel.read();
                }
            }
        });
        super.channelWritabilityChanged(ctx);
    }
}
