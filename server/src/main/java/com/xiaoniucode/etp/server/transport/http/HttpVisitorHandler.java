package com.xiaoniucode.etp.server.transport.http;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.core.enums.ProtocolType;
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

/**
 * 处理来自公网访问者的请求
 */
@Component
@ChannelHandler.Sharable
public class HttpVisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(HttpVisitorHandler.class);
    @Autowired
    private StreamManager visitorManager;
    @Autowired
    @Qualifier("streamStateMachine")
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel visitor = ctx.channel();
        Optional<StreamContext> contextOpt = visitorManager.getStreamContext(visitor);
        if (contextOpt.isPresent()) {
            StreamContext context = contextOpt.get();
            if (context.getState() == StreamState.OPENED) {
                ByteBuf payload = buf.retain();
                context.relayToTunnel(payload);
            } else {
                logger.error("隧道未开启，无法传输数据");
            }

        } else {
            logger.debug("[HTTP] 创建流上下文");
            buf.retain();
            visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).set(buf);
            StreamContext streamContext = visitorManager.createStreamContext(visitor,stateMachine);
            streamContext.setCurrentProtocol(ProtocolType.HTTP);
            streamContext.fireEvent(StreamEvent.STREAM_OPEN);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel visitor = ctx.channel();
        visitorManager.getStreamContext(visitor).ifPresent(streamContext -> {
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        visitorManager.getStreamContext(ctx.channel()).ifPresent(streamContext -> {
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
        });
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitorManager.getStreamContext(visitor).ifPresent(streamContext -> {
            Channel tunnel = streamContext.getTunnel();
            if (tunnel != null) {
                tunnel.config().setOption(ChannelOption.AUTO_READ, visitor.isWritable());
            }
        });
        super.channelWritabilityChanged(ctx);
    }
}
