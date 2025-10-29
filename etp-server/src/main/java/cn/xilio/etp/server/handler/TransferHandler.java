package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage.Message;
import cn.xilio.etp.core.AbstractMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 将从客户端代理接收到的数据转发给公网访问者
 *
 * @author liuxin
 */
public class TransferHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        Channel visitorChannel = ctx.channel().attr(EtpConstants.CLIENT_CHANNEL).get();
        if (visitorChannel == null || !visitorChannel.isWritable()) {
            return;
        }
        ByteBuf buf = Unpooled.wrappedBuffer(msg.getPayload().asReadOnlyByteBuffer());
        visitorChannel.writeAndFlush(buf);
    }
}
