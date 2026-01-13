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
        try {
            int totalLength = in.readInt();
            if (totalLength < 1 || in.readableBytes() < totalLength) {
                in.resetReaderIndex();
                return;
            }
            byte messageType = in.readByte();

            //读取消息体
            int bodyLength = totalLength - 1; // 总长度 - 消息类型(1)
            ByteBuf bodyBuf = in.readSlice(bodyLength);
            MessageSerializer<?> serializer = SerializerFactory.getSerializer(messageType);
            if (serializer == null) {
                throw new IllegalArgumentException("serializer not found");
            }
            Object message = serializer.deserialize(bodyBuf);
            if (message != null) {
                out.add(message);
            }
        } catch (Exception e) {
            in.resetReaderIndex();
            logger.error("decode error", e);
        }
    }
}
