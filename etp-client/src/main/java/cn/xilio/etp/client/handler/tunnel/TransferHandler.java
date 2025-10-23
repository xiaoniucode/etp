package cn.xilio.etp.client.handler.tunnel;

import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TransferHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        Channel realChannel = ctx.channel().attr(EtpConstants.NEXT_CHANNEL).get();
        ByteString data = msg.getPayload();
        ByteBuf buffer = Unpooled.wrappedBuffer(data.asReadOnlyByteBuffer());
        realChannel.writeAndFlush(buffer);
    }
}
