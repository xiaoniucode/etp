package com.xiaoniucode.etp.core.codec.compress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.SnappyFrameEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Snappy压缩编码器
 */
public class SnappyEncoder extends SnappyFrameEncoder {
    private static final Logger logger = LoggerFactory.getLogger(SnappyEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        if (logger.isDebugEnabled()) {
            int beforeCompressSize = in.readableBytes();
            int initialOutSize = out.writerIndex();

            super.encode(ctx, in, out);

            int afterCompressSize = out.writerIndex() - initialOutSize;
            if (beforeCompressSize > 0) {
                if (afterCompressSize < beforeCompressSize) {
                    // 压缩率 = (1 - 压缩后大小 / 压缩前大小) * 100%
                    double compressionRatio = (1 - (double) afterCompressSize / beforeCompressSize) * 100;
                    logger.debug("压缩前: {} 字节, 压缩后: {} 字节, 压缩率: {}%",
                            beforeCompressSize,
                            afterCompressSize,
                            String.format("%.2f", compressionRatio));
                } else {
                    // 膨胀率 = (压缩后大小 / 压缩前大小 - 1) * 100%
                    double expansionRatio = ((double) afterCompressSize / beforeCompressSize - 1) * 100;
                    logger.debug("压缩前 {} 字节, 压缩后 {} 字节, 膨胀率 {}%",
                            beforeCompressSize,
                            afterCompressSize,
                            String.format("%.2f", expansionRatio));
                }
            }
        } else {
            super.encode(ctx, in, out);
        }
    }
}
