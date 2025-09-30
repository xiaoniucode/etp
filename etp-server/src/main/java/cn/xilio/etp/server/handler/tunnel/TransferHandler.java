package cn.xilio.etp.server.handler.tunnel;

import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.core.AbstractMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 传输消息处理器
 */
public class TransferHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx,
                            TunnelMessage.Message msg) {
        Channel visitorChannel = ctx.channel().attr(EtpConstants.NEXT_CHANNEL).get();
        if (visitorChannel == null || !visitorChannel.isWritable()) {
            return;
        }
        ByteBuf buf = Unpooled.wrappedBuffer(msg.getPayload().asReadOnlyByteBufferList().get(0));
        visitorChannel.writeAndFlush(buf, visitorChannel.voidPromise());
    }
}
