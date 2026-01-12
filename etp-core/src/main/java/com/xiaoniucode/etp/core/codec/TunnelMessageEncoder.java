package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TunnelMessageEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg == null) {
            return;
        }
        if (msg instanceof Message message) {
            MessageSerializer<Message> serializer = SerializerFactory.getSerializer(message);
            ByteBuf bodyBuf = ctx.alloc().ioBuffer();
            try {
                bodyBuf.writeChar(message.getType());
                serializer.serialize(message, bodyBuf);
                int totalLength = bodyBuf.readableBytes();
                out.writeInt(totalLength);
                out.writeBytes(bodyBuf);
            } finally {
                bodyBuf.release();
            }
        }
    }
}
