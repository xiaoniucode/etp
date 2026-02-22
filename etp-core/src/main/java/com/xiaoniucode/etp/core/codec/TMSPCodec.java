package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class TMSPCodec {
    /**
     * 长度字段偏移量：magic(4)+version(1)+msgType(1)+streamId(4)=10
     */
    public static final int LENGTH_FIELD_OFFSET = 10;
    /**
     * 长度字段字节数：4
     */
    public static final int LENGTH_FIELD_LENGTH = 4;

    public static class Decoder extends LengthFieldBasedFrameDecoder {
        public Decoder() {
            this(1024 * 1024); // 默认1MB
        }

        public Decoder(int maxFrameLength) {
            super(maxFrameLength, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, 0, 0);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            ByteBuf frame = (ByteBuf) super.decode(ctx, in);
            if (frame == null) {
                //数据包不足，等待下一次
                return null;
            }
            TMSPPacket packet = new TMSPPacket();
            int magic = frame.readInt();
            if (magic != TMSP.MAGIC) {
                // 释放无效帧
                frame.release();
                throw new CorruptedFrameException("Invalid magic: " + magic);
            }
            packet.setMagic(magic);
            packet.setVersion(frame.readByte());
            packet.setMsgType(frame.readByte());
            packet.setStreamId(frame.readInt());

            int length = frame.readInt();
            if (length < 0) {
                frame.release();
                throw new CorruptedFrameException("Negative length: " + length);
            }
            if (length > 0) {
                ByteBuf payload = frame.readSlice(length);
                payload.retain();
                packet.setPayload(payload);
            }
            return packet;
        }
    }

    public static class Encoder extends MessageToByteEncoder<TMSPPacket> {
        @Override
        protected void encode(ChannelHandlerContext ctx, TMSPPacket packet, ByteBuf out) {
            out.writeInt(packet.getMagic());
            out.writeByte(packet.getVersion());
            out.writeByte(packet.getMsgType());
            out.writeInt(packet.getStreamId());
            out.writeInt(packet.getLength());

            if (packet.getPayload() != null) {
                out.writeBytes(packet.getPayload());
            }
        }
    }
}