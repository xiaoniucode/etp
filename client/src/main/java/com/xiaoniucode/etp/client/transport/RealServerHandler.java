package com.xiaoniucode.etp.client.transport;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 *
 * @author liuxin
 */
public class RealServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(RealServerHandler.class);
    private final AgentContext agentContext;

    public RealServerHandler(AgentContext agentContext) {
        this.agentContext = agentContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Channel visitor = ctx.channel();
        Optional<StreamContext> streamCtx = StreamManager.getStreamContext(visitor);
        if (streamCtx.isPresent()) {
            StreamContext streamContext = streamCtx.get();
            if (streamContext.isMuxTunnel()) {
                ByteBuf payload = msg.retain();
                streamContext.relayToTunnel(payload);
            }
        } else {
            ctx.fireChannelRead(msg.retain());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //流断开 server
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel server = ctx.channel();
        StreamManager.getStreamContext(server).ifPresent(streamContext -> {
            Channel tunnel = streamContext.getTunnel();
            if (tunnel != null && tunnel.isActive()) {
                boolean shouldRead = tunnel.isWritable();
                tunnel.config().setOption(ChannelOption.AUTO_READ, shouldRead);

                if (logger.isDebugEnabled()) {
                    logger.debug("真实服务 writability changed, streamId={}, tunnel writable={}, set AUTO_READ={}",
                            streamContext.getStreamId(), tunnel.isWritable(), shouldRead);
                }
            }
        });
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        //server exe
        super.exceptionCaught(ctx, cause);
    }
}
