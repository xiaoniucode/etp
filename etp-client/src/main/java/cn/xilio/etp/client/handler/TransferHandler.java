package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.core.AbstractMessageHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 将公网代理接受到的数据写到内网真实服务
 * @author liuxin
 */
public class TransferHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        Channel realChannel = ctx.channel().attr(EtpConstants.REAL_SERVER_CHANNEL).get();
        ByteBuf buffer = Unpooled.wrappedBuffer(msg.getPayload().asReadOnlyByteBuffer());
        realChannel.writeAndFlush(buffer);
    }
}
