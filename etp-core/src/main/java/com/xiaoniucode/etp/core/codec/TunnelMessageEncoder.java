package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelMessageEncoder extends MessageToByteEncoder<Object> {
    private final Logger logger = LoggerFactory.getLogger(TunnelMessageEncoder.class);

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
                int totalLength = 1 + bodyBuf.readableBytes();
                out.writeInt(totalLength);
                out.writeByte(message.getType());
                out.writeBytes(bodyBuf.retain());
            } catch (Exception e) {
                bodyBuf.release();
                logger.error(e.getMessage(), e);
            }
        }
    }
}
