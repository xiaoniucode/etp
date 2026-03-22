package com.xiaoniucode.etp.server.transport.tcp;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
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
public class TcpVisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(TcpVisitorHandler.class);
    @Autowired
    private StreamManager visitorManager;
    @Autowired
    @Qualifier("streamStateMachine")
    private StateMachine<StreamState, StreamEvent, StreamContext> stateMachine;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("[TCP]收到访问者请求");
        Channel visitor = ctx.channel();
        StreamContext streamContext = visitorManager.createStreamContext(visitor, stateMachine);
        streamContext.setCurrentProtocol(ProtocolType.TCP);
        streamContext.fireEvent(StreamEvent.STREAM_OPEN);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        Channel visitor = ctx.channel();
        Optional<StreamContext> contextOpt = visitorManager.getStreamContext(visitor);
        contextOpt.ifPresent(streamContext -> streamContext.forwardToLocal(msg));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        visitorManager.getStreamContext(ctx.channel()).ifPresent(streamContext -> {
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
        });
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitorManager.getStreamContext(visitor).ifPresent(streamContext -> {
            Channel tunnel = streamContext.getTunnel();
            if (tunnel != null && tunnel.isActive()) {
                boolean writable = visitor.isWritable();
                tunnel.config().setOption(ChannelOption.AUTO_READ, writable);
            }
        });
        super.channelWritabilityChanged(ctx);
    }
}
