package cn.xilio.etp.core.protocol;

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author liuxin
 */
public class TunnelMessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final byte HEADER_SIZE = 4;
    private static final int TYPE_SIZE = 1;
    private static final int PORT_SIZE = 4;
    private static final int EXT_SIZE = 1;
    private static final int SESSION_ID_SIZE = 8;

    public TunnelMessageDecoder() {
        super(2 * 1024 * 1024, 0, 4, 0, 0);
    }

    @Override
    protected TunnelMessage.Message decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        ByteBuf in = (ByteBuf) super.decode(ctx, buf);
        if (in == null || in.readableBytes() < HEADER_SIZE) {
            return null;
        }
        int frameLength = in.readInt();
        if (in.readableBytes() < frameLength) {
            return null;
        }
        TunnelMessage.Message.Builder message = TunnelMessage.Message.newBuilder();
        message.setType(TunnelMessage.Message.Type.forNumber(in.readByte()));
        message.setSessionId(in.readLong());
        message.setPort(in.readInt());
        byte extLength = in.readByte();
        byte[] extBytes = new byte[extLength];
        in.readBytes(extBytes);
        message.setExt(new String(extBytes));
        int dataLength = frameLength - TYPE_SIZE - SESSION_ID_SIZE - PORT_SIZE - EXT_SIZE - extLength;
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        message.setPayload(ByteString.copyFrom(data));
        in.release();
        return message.build();
    }
}
