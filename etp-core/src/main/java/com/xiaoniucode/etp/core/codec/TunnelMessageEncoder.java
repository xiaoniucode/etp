package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TunnelMessageEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        if (msg == null) {
            return;
        }
        if (msg instanceof Message message) {
            MessageSerializer<Message> serializer = SerializerFactory.getSerializer(message);
            ByteBuf bodyBuf = ctx.alloc().ioBuffer();
            try {
                serializer.serialize(message, bodyBuf);
                //计算总长度：消息类型(1) + 消息体长度
                int totalLength =1 + bodyBuf.readableBytes();
                out.writeInt(totalLength);
                out.writeByte(message.getType());
                out.writeBytes(bodyBuf);
            } finally {
                bodyBuf.release();
            }
        }
    }
}
