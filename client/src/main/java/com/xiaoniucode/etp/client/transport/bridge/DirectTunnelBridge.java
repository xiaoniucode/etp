package com.xiaoniucode.etp.client.transport.bridge;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.core.transport.IntSet;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.PipelineConfigure;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DirectTunnelBridge implements TunnelBridge {
    private final Logger logger = LoggerFactory.getLogger(DirectTunnelBridge.class);
    private final StreamContext streamContext;
    private final Channel tunnel;
    private final Channel server;

    public DirectTunnelBridge(StreamContext streamContext) {
        this.streamContext = streamContext;
        this.tunnel = streamContext.getTunnelEntry().getChannel();
        this.server = streamContext.getServer();
    }

    @Override
    public void open() {
        ChannelPipeline pipeline = tunnel.pipeline();
        PipelineConfigure.removeControlHandler(tunnel);
        pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                streamContext.forwardToLocal(msg);
            }

            @Override
            public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                logger.debug("隧道可写状态发生变化：{}", ctx.channel().isWritable());
                Channel tunnel = ctx.channel();
                boolean writable = tunnel.isWritable();
                if (writable) {
                    //数据隧道恢复可写，恢复暂停的从服务器读取
                    IntSet pausedStreamIds = StreamManager.getPausedStreamIds(tunnel);
                    if (!pausedStreamIds.isEmpty()) {
                        logger.debug("控制隧道恢复可写，恢复 {} 个访问者读取", pausedStreamIds.size());
                        pausedStreamIds.stream().forEach(streamId -> {
                            Optional<StreamContext> streamContextOpt = StreamManager.getStreamContext(streamId);
                            if (streamContextOpt.isPresent()) {
                                StreamContext streamContext = streamContextOpt.get();
                                Channel server = streamContext.getServer();
                                if (server != null) {
                                    ctx.executor().schedule(() -> {
                                        server.config().setOption(ChannelOption.AUTO_READ, true);
                                        server.read();
                                        StreamManager.removePausedStream(tunnel, streamId);
                                    }, 5, TimeUnit.MILLISECONDS);
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
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        if (streamContext.isChannelClosed(server)) {
            logger.error("真实服务连接没有激活，关闭流：streamId={}", streamContext.getStreamId());
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        server.writeAndFlush(payload.retain()).addListener((ChannelFutureListener) f -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
            if (f.isSuccess()) {
                logger.debug("数据成功转发给真实服务成功");
            } else {
                logger.debug("数据转发给真实服务失败",f.cause());
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            }
        });
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        if (streamContext.isChannelClosed(tunnel)) {
            logger.error("隧道没有激活，关闭流：streamId={}", streamContext.getStreamId());
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }
        tunnel.writeAndFlush(payload.retain()).addListener((ChannelFutureListener) f -> {
            logger.debug("流 {} 引用计数为：{}", streamContext.getStreamId(), payload.refCnt());
            if (f.isSuccess()) {
                logger.debug("数据成功转发到远程成功");
            } else {
                logger.debug("数据转发到远程失败",f.cause());
            }
        });
    }
}