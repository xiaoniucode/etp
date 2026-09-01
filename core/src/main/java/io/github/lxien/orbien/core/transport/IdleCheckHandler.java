package io.github.lxien.orbien.core.transport;

import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 空闲检查
 */
public class IdleCheckHandler extends IdleStateHandler {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(IdleCheckHandler.class);

    public IdleCheckHandler() {
        super(300, 180, 0, TimeUnit.SECONDS);
    }

    public IdleCheckHandler(long readerIdleSeconds, long writerIdleSeconds) {
        super(readerIdleSeconds, writerIdleSeconds, 0, TimeUnit.SECONDS);
    }

    public static IdleCheckHandler forDataTunnel() {
        return new IdleCheckHandler(300, 0);
    }

    public static IdleCheckHandler forMultiplexTunnel() {
        return new IdleCheckHandler(90, 0);
    }

    public static IdleCheckHandler forVisitor() {
        return new IdleCheckHandler(300, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        Channel channel = ctx.channel();
        switch (evt.state()) {
            case WRITER_IDLE:
                logger.debug("写超时，关闭连接 {}", channel.remoteAddress());
                ChannelUtils.closeOnFlush(channel);
                break;

            case READER_IDLE:
                logger.debug("读空闲超时，关闭连接 {}", channel.remoteAddress());
                ChannelUtils.closeOnFlush(channel);
                break;
            default:
                break;
        }
        ctx.fireUserEventTriggered(evt);
    }
}
