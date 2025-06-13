package cn.xilio.vine.core.protocol;

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TunnelMessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final byte MAXFRAMELENGTH = 4;
    private static final byte HEADER_SIZE = 4;

    private static final int TYPE_SIZE = 1;

    private static final int SERIAL_NUMBER_SIZE = 8;

    private static final int URI_LENGTH_SIZE = 1;


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
        TunnelMessage.Message.Builder proxyMessage = TunnelMessage.Message.newBuilder();
        byte type = in.readByte();
        long sn = in.readLong();
        proxyMessage.setType(TunnelMessage.Message.Type.forNumber(type));
        proxyMessage.setSerialNumber(sn);


        byte uriLength = in.readByte();
        byte[] uriBytes = new byte[uriLength];
        in.readBytes(uriBytes);
        proxyMessage.setUri(new String(uriBytes));

        byte[] data = new byte[frameLength - TYPE_SIZE - SERIAL_NUMBER_SIZE - URI_LENGTH_SIZE - uriLength];
        in.readBytes(data);
        proxyMessage.setData(ByteString.copyFrom(data));

        in.release();

        return proxyMessage.build();
    }
}
