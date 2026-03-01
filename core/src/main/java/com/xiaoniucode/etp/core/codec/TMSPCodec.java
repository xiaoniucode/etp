package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class TMSPCodec {
    /**
     * 长度字段偏移量：magic(4)+version(1)+msgType(1)+flags(1)+streamId(4)=11
     */
    public static final int LENGTH_FIELD_OFFSET = 11;
    /**
     * 长度字段字节数：4
     */
    public static final int LENGTH_FIELD_LENGTH = 4;

    public static ChannelHandler create(int maxFrameLength) {
        return new CombinedChannelDuplexHandler<>(
                new TMSPCodec.Decoder(maxFrameLength),
                new TMSPCodec.Encoder()
        );
    }

    public static class Decoder extends LengthFieldBasedFrameDecoder {
        public Decoder(int maxFrameLength) {
            super(maxFrameLength, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, 0, 0);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
            if (byteBuf == null) {
                //数据包不足，等待下一次
                return null;
            }

            int magic = byteBuf.readInt();
            if (magic != TMSP.MAGIC) {
                // 释放无效帧
                byteBuf.release();
                throw new CorruptedFrameException("Invalid magic: " + magic);
            }
            byte version = byteBuf.readByte();
            byte msgType = byteBuf.readByte();
            int streamId = byteBuf.readInt();
            byte flags = byteBuf.readByte();
            TMSPFrame frame = new TMSPFrame(streamId, msgType);
            frame.setMagic(magic);
            frame.setVersion(version);
            frame.setFlags(flags);

            int length = byteBuf.readInt();
            if (length < 0) {
                byteBuf.release();
                throw new CorruptedFrameException("Negative length: " + length);
            }
            if (length > 0) {
                ByteBuf payload = byteBuf.readSlice(length);
                payload.retain();
                frame.setPayload(payload);
            }
            return frame;
        }
    }

    public static class Encoder extends MessageToByteEncoder<TMSPFrame> {
        @Override
        protected void encode(ChannelHandlerContext ctx, TMSPFrame frame, ByteBuf out) {
            out.writeInt(frame.getMagic());
            out.writeByte(frame.getVersion());
            out.writeByte(frame.getMsgType());
            out.writeInt(frame.getStreamId());
            out.writeByte(frame.getFlags());
            int length = frame.getPayload() != null ? frame.getPayload().readableBytes() : 0;
            out.writeInt(length);
            if (length > 0) {
                out.writeBytes(frame.getPayload());
            }
        }
    }
}