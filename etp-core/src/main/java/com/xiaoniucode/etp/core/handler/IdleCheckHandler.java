package com.xiaoniucode.etp.core.handler;

import com.xiaoniucode.etp.core.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 心跳检查
 *
 * @author liuxin
 */
public class IdleCheckHandler extends IdleStateHandler {
    public IdleCheckHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT == evt) {
            Message.MessageHeader header = Message.MessageHeader.newBuilder().setType(Message.MessageType.PING).build();
            Message.Ping ping = Message.Ping.newBuilder().build();
            Message.ControlMessage message = Message.ControlMessage.newBuilder().setHeader(header).setPing(ping).build();
            ctx.channel().writeAndFlush(message);
        } else if (IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT == evt) {
            ctx.channel().close();
        }
        super.channelIdle(ctx, evt);
    }
}
