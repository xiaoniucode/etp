package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

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
                ByteBuf compressedBodyBuf = compressData(bodyBuf);
                int totalLength = 1 + compressedBodyBuf.readableBytes();
                out.writeInt(totalLength);
                out.writeByte(message.getType());
                out.writeBytes(compressedBodyBuf);
                compressedBodyBuf.release();

            } catch (Exception e) {
               logger.error(e.getMessage(), e);
            } finally {
                bodyBuf.release();
            }
        }
    }

    private ByteBuf compressData(ByteBuf originalData) {
        if (originalData == null || !originalData.isReadable()) {
            assert originalData != null;
            return originalData.retain();
        }
        int originalLength = originalData.readableBytes();
        try {
            byte[] dataToCompress = new byte[originalLength];
            originalData.getBytes(originalData.readerIndex(), dataToCompress);
            byte[] compressedData = Snappy.compress(dataToCompress);
            
            if (compressedData.length >= originalLength) {
                logger.debug("压缩前大小: {}, 压缩后大小: {}, 压缩率: {}", originalLength, compressedData.length, "无效");
                originalData.resetReaderIndex();
                return originalData.retain();
            }
            
            logger.debug("压缩前大小: {}, 压缩后大小: {}, 压缩率: {}%", originalLength, compressedData.length, 
                String.format("%.2f", (1 - (double)compressedData.length / originalLength) * 100));
            return Unpooled.wrappedBuffer(compressedData);

        } catch (Exception e) {
            originalData.resetReaderIndex();
            return originalData.retain();
        }
    }
}
