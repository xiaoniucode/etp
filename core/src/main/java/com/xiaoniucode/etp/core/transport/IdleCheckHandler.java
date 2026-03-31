package com.xiaoniucode.etp.core.transport;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 心跳检查
 *
 * @author liuxin
 */
public class IdleCheckHandler extends IdleStateHandler {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(IdleCheckHandler.class);

    public IdleCheckHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        switch (evt.state()) {
            case WRITER_IDLE:
               ctx.channel().writeAndFlush(new TMSPFrame(0, TMSP.MSG_PING));
                logger.debug("发送心跳包给 {}", ctx.channel().remoteAddress());
                break;

            case READER_IDLE:
                logger.warn("读空闲超时，即将关闭连接 {}", ctx.channel().remoteAddress());
               //todo ctx.channel().close();
                break;
            case ALL_IDLE:
                logger.debug("读写空闲超时，即将关闭连接 {}", ctx.channel().remoteAddress());
              //todo  ctx.channel().close();
                break;
        }

        super.channelIdle(ctx, evt);
    }
}
