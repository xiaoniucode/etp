package cn.xilio.etp.core.protocol;

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author xiaoniucode
 */
public class TunnelMessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final byte HEADER_SIZE = 4;
    private static final int TYPE_SIZE = 1;
    private static final int PORT_SIZE=4;
    private static final int EXT_SIZE = 1;
    private static final int SESSION_ID_SIZE = 8;

    public TunnelMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
                                int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected TunnelMessage.Message decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        ByteBuf in = (ByteBuf) super.decode(ctx, buf);
        if (in == null) {
            return null;
        }
        if (in.readableBytes() < HEADER_SIZE) {
            return null;
        }
        int frameLength = in.readInt();
        if (in.readableBytes() < frameLength) {
            return null;
        }
        TunnelMessage.Message.Builder tunnelMessage = TunnelMessage.Message.newBuilder();
        byte type = in.readByte();
        tunnelMessage.setType(TunnelMessage.Message.Type.forNumber(type));

        long sessionId = in.readLong();
        tunnelMessage.setSessionId(sessionId);
        int port = in.readInt();
        tunnelMessage.setPort(port);

        byte extLength = in.readByte();
        byte[] extBytes = new byte[extLength];
        in.readBytes(extBytes);
        tunnelMessage.setExt(new String(extBytes));

        byte[] data = new byte[frameLength - TYPE_SIZE - SESSION_ID_SIZE -PORT_SIZE- EXT_SIZE - extLength];
        in.readBytes(data);
        tunnelMessage.setPayload(ByteString.copyFrom(data));
        in.release();
        return tunnelMessage.build();
    }
}
