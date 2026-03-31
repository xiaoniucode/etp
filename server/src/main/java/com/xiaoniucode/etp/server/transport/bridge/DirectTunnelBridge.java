package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.transport.IntSet;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.PipelineConfigure;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

public class DirectTunnelBridge implements TunnelBridge {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(DirectTunnelBridge.class);
    private final StreamContext streamContext;
    private final Channel visitor;
    private final Channel tunnel;

    public DirectTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
        this.visitor = streamContext.getVisitor();
        this.tunnel = streamContext.getTunnelEntry().getChannel();
    }

    @Override
    public void open() {
        ChannelPipeline pipeline = tunnel.pipeline();
        PipelineConfigure.removeControlHandler(tunnel);
        pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                streamContext.forwardToRemote(msg);
            }

            @Override
            public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                StreamManager streamManager = streamContext.getStreamManager();
                logger.warn("隧道可写状态发生变化：{}", ctx.channel().isWritable());
                Channel tunnel = ctx.channel();
                boolean writable = tunnel.isWritable();
                logger.warn("控制隧道可写性变化：{}", writable);
                if (writable) {
                    //数据隧道恢复可写，恢复暂停的访问者读取
                    IntSet pausedStreamIds = streamManager.getPausedStreamIds(tunnel);
                    logger.debug("获取到 {} 条暂停流数量", pausedStreamIds.size());
                    if (!pausedStreamIds.isEmpty()) {
                        logger.debug("控制隧道恢复可写，恢复 {} 个访问者读取", pausedStreamIds.size());
                        pausedStreamIds.stream().forEach(streamId -> {
                            StreamContext streamContext = streamManager.getStreamContext(streamId);
                            if (streamContext != null) {
                                Channel visitor = streamContext.getVisitor();
                                if (visitor != null) {
                                    ctx.executor().schedule(() -> {
                                        visitor.config().setOption(ChannelOption.AUTO_READ, true);
                                        visitor.read();
                                        streamManager.removePausedStream(tunnel, streamId);
                                    }, 5, TimeUnit.MILLISECONDS);//延迟5ms，避免隧道在临界状态下来回切换，防止"乒乓效应"
                                }
                            }
                        });
                    }
                }
                super.channelWritabilityChanged(ctx);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                logger.error("独立隧道传输发生异常",cause);
            }
        });
        logger.debug("独立隧道已经打开, 处理器: {}", pipeline.names());
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        if (streamContext.isChannelClosed(tunnel)) {
            logger.error("隧道没有激活：streamId={}", streamContext.getStreamId());
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        tunnel.writeAndFlush(payload.retain()).addListener((ChannelFutureListener) f -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
            if (f.isSuccess()) {
                logger.debug("数据转发到内网成功：streamId={}", streamContext.getStreamId());
            } else {
                logger.debug("数据转发到内网失败：streamId={}", streamContext.getStreamId(),f.cause());
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            }
        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        if (streamContext.isChannelClosed(visitor)) {
            logger.error("访问者通道没有激活：streamId={}", streamContext.getStreamId());
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        visitor.writeAndFlush(payload.retain()).addListener((ChannelFutureListener) f -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
            if (f.isSuccess()) {
                logger.debug("数据转发给访问者成功：streamId={}", streamContext.getStreamId());
            } else {
                logger.debug("数据转发给访问者失败：streamId={}", streamContext.getStreamId(), f.cause());
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            }
        });
    }
}