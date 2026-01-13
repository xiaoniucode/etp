package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TunnelMessageDecoder extends ByteToMessageDecoder {

    private Logger logger = LoggerFactory.getLogger(TunnelMessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) {
            return;
        }
        in.markReaderIndex();
        int totalLength = in.readInt();
        if (totalLength < 1 || in.readableBytes() < totalLength) {
            in.resetReaderIndex();
            return;
        }
        byte messageType = in.readByte();
        int bodyLength = totalLength - 1;
        ByteBuf bodyBuf = in.readRetainedSlice(bodyLength);
        try {
            MessageSerializer<?> serializer = SerializerFactory.getSerializer(messageType);
            if (serializer == null) {
                throw new IllegalArgumentException("serializer not found");
            }
            Object message = serializer.deserialize(bodyBuf);
            if (message != null) {
                out.add(message);
            }
        } catch (Exception e) {
            bodyBuf.release();
            in.resetReaderIndex();
            logger.error("decode error", e);
        }
    }
}
