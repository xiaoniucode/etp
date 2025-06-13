package cn.xilio.vine.client.handler.internal;

import cn.xilio.vine.core.Constants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RealChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        Channel tunnelChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        String visitorId="1001";
        byte[] dataBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(dataBytes);

        TunnelMessage.Message message = TunnelMessage.Message
                .newBuilder()
                .setType(TunnelMessage.Message.Type.TRANSFER)
                .setExt(visitorId)
                .setPayload(ByteString.copyFrom(dataBytes))
                .build();
        tunnelChannel.writeAndFlush(message);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
       cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
