package cn.xilio.etp.core.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TunnelMessageEncoder extends MessageToByteEncoder<TunnelMessage.Message> {
    private static final int TYPE_SIZE = 1;
    private static final int SESSION_ID_SIZE = 8;
    private static final int PORT_SIZE = 4;
    private static final int EXT_SIZE = 1;

    @Override
    protected void encode(ChannelHandlerContext ctx, TunnelMessage.Message msg, ByteBuf out) throws Exception {
        int bodyLength = TYPE_SIZE + SESSION_ID_SIZE + PORT_SIZE + EXT_SIZE;
        byte[] extBytes = msg.getExt().getBytes();
        bodyLength += extBytes.length;
        bodyLength += msg.getPayload().size();

        out.writeInt(bodyLength);
        out.writeByte(msg.getType().getNumber());
        out.writeLong(msg.getSessionId());
        out.writeInt(msg.getPort());
        out.writeByte((byte) extBytes.length);
        out.writeBytes(extBytes);
        out.writeBytes(msg.getPayload().toByteArray());
    }
}
