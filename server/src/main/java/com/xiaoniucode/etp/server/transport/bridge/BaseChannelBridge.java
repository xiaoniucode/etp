package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseChannelBridge extends ChannelDuplexHandler implements ChannelBridge {
    private static final Logger logger = LoggerFactory.getLogger(BaseChannelBridge.class);
    protected final Channel target;
    protected final String direction;
    protected final StreamContext streamContext;

    public BaseChannelBridge(StreamContext streamContext, Channel target, String direction) {
        this.streamContext = streamContext;
        this.target = target;
        this.direction = direction;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!target.isActive() || !target.isOpen() || !target.isWritable()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        ReferenceCountUtil.retain(msg);
        target.writeAndFlush(msg).addListener((ChannelFutureListener) f -> {
            try {
                if (!f.isSuccess()) {
                    logger.error("消息转发失败", f.cause());
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("数据转发异常: 数据流方向={}", direction, cause);
        streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public Channel getTarget() {
        return target;
    }
}