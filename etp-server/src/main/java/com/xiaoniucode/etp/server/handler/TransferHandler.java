package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.protocol.TunnelMessage.Message;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 将从客户端接收到的数据转发给公网访问者
 *
 * @author liuxin
 */
public class TransferHandler extends AbstractTunnelMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        Channel dataChannel = ctx.channel();
        Channel visitorChannel = dataChannel.attr(EtpConstants.VISITOR_CHANNEL).get();
        if (visitorChannel != null && visitorChannel.isActive()) {
            dataChannel.config().setAutoRead(visitorChannel.isWritable());
            ByteBuf buf = Unpooled.wrappedBuffer(msg.getPayload().asReadOnlyByteBuffer());
            visitorChannel.writeAndFlush(buf);
        }
    }
}
