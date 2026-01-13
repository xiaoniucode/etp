package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.core.serializer.MessageSerializer;
import com.xiaoniucode.etp.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.util.List;

public class TunnelMessageDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(TunnelMessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 6) {
            return;
        }
        in.markReaderIndex();
        int totalLength = in.readInt();
        if (totalLength < 2 || in.readableBytes() < totalLength) {
            in.resetReaderIndex();
            return;
        }
        byte messageType = in.readByte();
        byte useCompress = in.readByte();
        int bodyLength = totalLength - 2;
        ByteBuf bodyBuf = in.readRetainedSlice(bodyLength);

        try {
            boolean isCompressed = (useCompress & 0x01) != 0;
            ByteBuf finalBodyBuf;
            
            if (isCompressed) {
                finalBodyBuf = decompressData(bodyBuf, ctx);
            } else {
                finalBodyBuf = bodyBuf.retain();
            }
            
            MessageSerializer<?> serializer = SerializerFactory.getSerializer(messageType);
            if (serializer == null) {
                throw new IllegalArgumentException("serializer not found");
            }
            Object message = serializer.deserialize(finalBodyBuf);
            if (message != null) {
                out.add(message);
            }
            finalBodyBuf.release();
        } catch (Exception e) {
            bodyBuf.release();
            in.resetReaderIndex();
            logger.error("decode error", e);
        }
    }

    private ByteBuf decompressData(ByteBuf compressedData, ChannelHandlerContext ctx) {
        if (compressedData == null || !compressedData.isReadable()) {
            assert compressedData != null;
            return compressedData.retain();
        }
        int compressedLength = compressedData.readableBytes();

        try {
            byte[] dataToDecompress = new byte[compressedLength];
            compressedData.getBytes(compressedData.readerIndex(), dataToDecompress);
            byte[] decompressedData = Snappy.uncompress(dataToDecompress);
            logger.debug("解压前大小: {}, 解压后大小: {}, 解压率: {}%", compressedLength, decompressedData.length,
                    String.format("%.2f", ((double) decompressedData.length / compressedLength) * 100));
            
            ByteBuf resultBuf = Unpooled.wrappedBuffer(decompressedData);
            resultBuf.readerIndex(0);
            return resultBuf;

        } catch (Exception e) {
            compressedData.resetReaderIndex();
            return compressedData.retain();
        }
    }
}
