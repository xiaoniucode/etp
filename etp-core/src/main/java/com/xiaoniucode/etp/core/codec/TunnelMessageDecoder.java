package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class TunnelMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) {
            return;
        }
        in.markReaderIndex();
        try {
            int totalLength = in.readInt();
            if (in.readableBytes() < totalLength) {
                in.resetReaderIndex();
                return;
            }
            char messageType = in.readChar();
            MessageSerializer<?> serializer = SerializerFactory.getSerializer(messageType);
            Message message = serializer.deserialize(in);
            out.add(message);
        } catch (Exception e) {
            in.resetReaderIndex();
            throw e;
        }
    }
}
