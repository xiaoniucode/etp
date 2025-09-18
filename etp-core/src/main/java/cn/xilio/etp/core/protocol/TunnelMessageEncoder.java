package cn.xilio.etp.core.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.util.ObjectUtils;

public class TunnelMessageEncoder extends MessageToByteEncoder<TunnelMessage.Message> {
    private static final int TYPE_SIZE = 1;
    private static final int SESSION_ID_SIZE = 8;
    private static final int EXT_SIZE = 1;

    @Override
    protected void encode(ChannelHandlerContext ctx, TunnelMessage.Message msg, ByteBuf out) throws Exception {
        int bodyLength = TYPE_SIZE + SESSION_ID_SIZE + EXT_SIZE;
        byte[] extBytes = null;
        if (!ObjectUtils.isEmpty(msg.getExt())) {
            extBytes = msg.getExt().getBytes();
            bodyLength += extBytes.length;
        }

        if (!ObjectUtils.isEmpty(msg.getPayload())) {
            bodyLength += msg.getPayload().size();
        }

        out.writeInt(bodyLength);
        out.writeByte(msg.getType().getNumber());
        out.writeLong(msg.getSessionId());
        if (!ObjectUtils.isEmpty(extBytes)) {
            out.writeByte((byte) extBytes.length);
            out.writeBytes(extBytes);
        } else {
            out.writeByte((byte) 0x00);
        }
        if (!ObjectUtils.isEmpty(msg.getPayload())) {
            out.writeBytes(msg.getPayload().toByteArray());
        }
    }
}
