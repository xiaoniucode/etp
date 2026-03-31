package com.xiaoniucode.etp.client.transport;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Optional;

/**
 *
 * @author liuxin
 */
public class RealServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(RealServerHandler.class);
    private final AgentContext agentContext;

    public RealServerHandler(AgentContext agentContext) {
        this.agentContext = agentContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        Channel server = ctx.channel();
        Optional<StreamContext> streamCtx = StreamManager.getStreamContext(server);
        streamCtx.ifPresent(streamContext -> {
                    TunnelEntry tunnelEntry = streamContext.getTunnelEntry();
                    Channel tunnel = tunnelEntry.getChannel();
                    if (!tunnel.isWritable()) {
                        logger.debug("数据无法转发到远程，流量过高，隧道不可写，暂停从服务读取，streamId={}", streamContext.getStreamId());
                        server.config().setOption(ChannelOption.AUTO_READ, false);
                        StreamManager.addPausedStreamId(tunnel, streamContext.getStreamId());
                    }
                    streamContext.forwardToRemote(msg);
                }
        );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel server = ctx.channel();
        Optional<StreamContext> streamCtx = StreamManager.getStreamContext(server);
        streamCtx.ifPresent(streamContext -> {
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
        });
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        logger.warn("服务端可写状态发生变化，当前状态：{}", ctx.channel().isWritable());
        Channel server = ctx.channel();
        StreamManager.getStreamContext(server).ifPresent(streamContext -> {
            Channel tunnel = streamContext.getTunnelEntry().getChannel();
            if (tunnel != null) {
                logger.warn("隧道流量过高，改变隧道的可读状态，无法写入服务器，当前隧道可写状态：{}", tunnel.isWritable());
                boolean shouldRead = server.isWritable();
                tunnel.config().setOption(ChannelOption.AUTO_READ, shouldRead);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
    }

}
