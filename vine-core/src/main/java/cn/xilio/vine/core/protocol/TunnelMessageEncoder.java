package cn.xilio.vine.core.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TunnelMessageEncoder extends MessageToByteEncoder<TunnelMessage.Message> {
    private static final int TYPE_SIZE = 1;
    private static final int EXT_LENGTH_SIZE = 1;
    @Override
    protected void encode(ChannelHandlerContext ctx, TunnelMessage.Message msg, ByteBuf out) throws Exception {
        int bodyLength = TYPE_SIZE  + EXT_LENGTH_SIZE;
        byte[] uriBytes = null;
        if (msg.getExt() != null) {
            uriBytes = msg.getExt().getBytes();
            bodyLength += uriBytes.length;
        }

        if (msg.getPayload() != null) {
            bodyLength += msg.getPayload().size();
        }

        out.writeInt(bodyLength);
        out.writeByte(msg.getType().getNumber());


        if (uriBytes != null) {
            out.writeByte((byte) uriBytes.length);
            out.writeBytes(uriBytes);
        } else {
            out.writeByte((byte) 0x00);
        }

        if (msg.getPayload() != null) {
            out.writeBytes(msg.getPayload().toByteArray());//todo
        }
    }
}
