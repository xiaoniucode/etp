package cn.xilio.vine.client.handler.tunnel;

import cn.xilio.vine.core.AbstractMessageHandler;
import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TransferHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        Channel realChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
        ByteString data = msg.getPayload();

        ByteBuf buffer = ctx.alloc().buffer(data.size());
        buffer.writeBytes(data.toByteArray());
        realChannel.writeAndFlush(buffer);
    }
}
