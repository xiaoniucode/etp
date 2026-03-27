package com.xiaoniucode.etp.core.transport.compress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.compression.CompressionException;
import io.netty.handler.codec.compression.Snappy;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.function.Consumer;

public class SnappyCompressor implements Compressor {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SnappyCompressor.class);
    protected static final int MIN_COMPRESSIBLE_LENGTH = 1024;
    private final Snappy snappy = new Snappy();

    @Override
    public void compress(Channel channel, ByteBuf in, Consumer<ByteBuf> consumer, int level) {
        int dataLength = in.readableBytes();
        if (dataLength == 0) {
            return;
        }

        if (dataLength > MIN_COMPRESSIBLE_LENGTH) {
            int remaining = dataLength;

            for (; ; ) {
                if (remaining < MIN_COMPRESSIBLE_LENGTH) {
                    //剩余数据太小不压缩
                    ByteBuf out = channel.alloc().buffer(1 + 3 + remaining);
                    out.writeByte(1);//1表示不压缩
                    out.writeMediumLE(remaining);
                    out.writeBytes(in, remaining);
                    consumer.accept(out);
                    break;
                }

                ByteBuf out = channel.alloc().buffer();

                out.writeByte(0); // 0表示压缩块
                // 记录长度字段位置
                int lengthIdx = out.writerIndex();
                // 预留长度字段空间
                out.writeMediumLE(0);

                if (remaining > Short.MAX_VALUE) {
                    ByteBuf slice = in.readSlice(Short.MAX_VALUE);
                    snappy.encode(slice, out, Short.MAX_VALUE);
                    setChunkLength(out, lengthIdx);
                    remaining -= Short.MAX_VALUE;
                    consumer.accept(out);
                } else {
                    ByteBuf slice = in.readSlice(remaining);
                    snappy.encode(slice, out, remaining);
                    setChunkLength(out, lengthIdx);
                    consumer.accept(out);
                    break;
                }
            }
        } else {
            ByteBuf out = channel.alloc().buffer(1 + 3 + dataLength);
            out.writeByte(1);//1表示不压缩
            out.writeMediumLE(dataLength);
            out.writeBytes(in, dataLength);
            consumer.accept(out);
        }
    }

    @Override
    public ByteBuf decompress(Channel channel, ByteBuf in) {
        if (in == null || !in.isReadable()) {
            return channel.alloc().buffer(0);
        }
        ByteBuf out = channel.alloc().buffer(Math.max(4096, in.readableBytes() * 2));
        try {
            while (in.isReadable()) {
                byte type = in.readByte();
                int dataLength = in.readMediumLE();

                if (logger.isDebugEnabled()) {
                    logger.debug("读取块: type={}, dataLength={}, 剩余可读={}",
                            type, dataLength, in.readableBytes());
                }

                // 验证数据长度是否合理
                if (dataLength < 0) {
                    throw new CompressionException("Invalid data length: " + dataLength);
                }

                if (dataLength > in.readableBytes()) {
                    throw new CompressionException(
                            String.format("Data length %d exceeds available bytes %d",
                                    dataLength, in.readableBytes()));
                }

                if (type == 0) {
                    ByteBuf compressedData = in.slice(in.readerIndex(), dataLength);
                    try {
                        snappy.reset();//清除状态
                        int beforeDecode = out.writerIndex();
                        snappy.decode(compressedData, out);
                        int decodedLength = out.writerIndex() - beforeDecode;

                        if (logger.isTraceEnabled()) {
                            logger.trace("解压块: 压缩大小={}, 解压后大小={}", dataLength, decodedLength);
                        }

                    } catch (Exception e) {
                        logger.error("解压失败: 压缩数据大小={}, 数据长度={}", compressedData.readableBytes(), dataLength, e);
                        throw e;
                    }
                    // 移动 readerIndex
                    in.readerIndex(in.readerIndex() + dataLength);

                } else if (type == 1) {
                    // 未压缩块直接复制
                    out.writeBytes(in, dataLength);

                    if (logger.isTraceEnabled()) {
                        logger.trace("未压缩块: 大小={}", dataLength);
                    }

                } else {
                    // 未知类型
                    throw new CompressionException("Unknown chunk type: " + type);
                }
            }

            return out;

        } catch (Exception e) {
            out.release();
            throw new CompressionException("数据解压失败", e);
        }
    }

    private static void setChunkLength(ByteBuf out, int lengthIdx) {
        int chunkLength = out.writerIndex() - lengthIdx - 3;
        if (chunkLength >>> 24 != 0) {
            throw new CompressionException("compressed data too large: " + chunkLength);
        }
        out.setMediumLE(lengthIdx, chunkLength);
    }
}