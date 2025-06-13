package cn.xilio.vine.core.protocol;

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TunnelMessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final byte HEADER_SIZE = 4;
    private static final int TYPE_SIZE = 1;
    private static final int EXT_LENGTH_SIZE = 1;
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
        proxyMessage.setType(TunnelMessage.Message.Type.forNumber(type));
        byte uriLength = in.readByte();
        byte[] uriBytes = new byte[uriLength];
        in.readBytes(uriBytes);
        proxyMessage.setExt(new String(uriBytes));

        byte[] data = new byte[frameLength - TYPE_SIZE  - EXT_LENGTH_SIZE - uriLength];
        in.readBytes(data);
        proxyMessage.setPayload(ByteString.copyFrom(data));//todo 需要优化
        in.release();
        return proxyMessage.build();
    }
}
