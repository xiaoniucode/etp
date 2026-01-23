package com.xiaoniucode.etp.core.codec.compress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.SnappyFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Snappy压缩解码器
 */
public class SnappyDecoder extends SnappyFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(SnappyDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (logger.isDebugEnabled()) {
            int beforeDecompressSize = in.readableBytes();
            int initialOutSize = out.size();

            super.decode(ctx, in, out);

            int afterDecompressSize = 0;
            if (out.size() > initialOutSize) {
                for (int i = initialOutSize; i < out.size(); i++) {
                    Object obj = out.get(i);
                    if (obj instanceof ByteBuf) {
                        afterDecompressSize += ((ByteBuf) obj).readableBytes();
                    }
                }
            }

            if (beforeDecompressSize > 0) {
                logger.debug("解压前: {} 字节, 解压后: {} 字节", beforeDecompressSize, afterDecompressSize);
            }
        } else {
            super.decode(ctx, in, out);
        }
    }
}
