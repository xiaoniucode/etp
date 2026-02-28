package com.xiaoniucode.etp.server.transport.tcp;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.VisitorManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理来自公网访问者的请求
 */
@Component
@ChannelHandler.Sharable
public class TcpVisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(TcpVisitorHandler.class);
    @Autowired
    private VisitorManager visitorManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("[TCP]收到访问者请求");
        Channel visitor = ctx.channel();
        StreamContext streamContext = visitorManager.createStreamContext(visitor);
        streamContext.setCurrentProtocol(ProtocolType.TCP);
        streamContext.fireEvent(StreamEvent.STREAM_OPEN);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        Channel visitor = ctx.channel();
        ByteBuf payload = msg.retain();
        visitorManager.getStreamContext(visitor).ifPresent(streamContext -> {
            streamContext.relayToTunnel(payload);
        });
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
                // visitor 的写缓冲区状态  -->  控制 tunnel 的读取
                boolean writable = visitor.isWritable();
                tunnel.config().setOption(ChannelOption.AUTO_READ, writable);
            }
        });
        super.channelWritabilityChanged(ctx);
    }
}
