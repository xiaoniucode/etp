/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.core.transport.compress;

import com.xiaoniucode.etp.core.message.TMSPFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.compression.CompressionException;
import io.netty.handler.codec.compression.Snappy;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class MultiplexSnappyDecoder extends SimpleChannelInboundHandler<TMSPFrame> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MultiplexSnappyDecoder.class);

    private final Snappy snappy = new Snappy();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMSPFrame frame) throws Exception {
        ByteBuf payload = frame.getPayload();
        if (payload == null || !payload.isReadable()) {
            ctx.fireChannelRead(frame);
            return;
        }
        // 如果不是 Snappy 压缩，直接透传
        if (!frame.isSnappy()) {
            ctx.fireChannelRead(frame);
            return;
        }

        ByteBuf decompressed = null;
        try {
            if (payload.readableBytes() < 4) {
                // 数据不足，等待下次
                ctx.fireChannelRead(frame);
                return;
            }

            int start = payload.readerIndex();
            byte type = payload.readByte();
            int dataLength = payload.readMediumLE();

            if (dataLength < 0 || payload.readableBytes() < dataLength) {
                payload.readerIndex(start);
                ctx.fireChannelRead(frame);
                return;
            }

            decompressed = ctx.alloc().buffer(Math.max(8192, dataLength * 2));

            if (type == 0) {
                snappy.reset();
                ByteBuf slice = payload.readSlice(dataLength);
                logger.debug("解压前 payload refCnt: {}", payload.refCnt());
                snappy.decode(slice, decompressed);
                logger.debug("解压后 decompressed refCnt: {}", decompressed.refCnt());
            } else if (type == 1) {
                decompressed.writeBytes(payload, dataLength);
            } else {
                throw new CompressionException("Unknown type: " + type);
            }

            // 先保存旧 payload，避免替换后丢失引用导致泄漏
            ByteBuf oldPayload = payload;
            // 替换为解压后的 payload
            frame.setPayload(decompressed);
            logger.debug("替换后 frame payload refCnt: {}", frame.getPayload().refCnt());
            oldPayload.release();
            ctx.fireChannelRead(frame);

        } catch (Exception e) {
            if (decompressed != null) decompressed.release();
            logger.error("Snappy 解压失败 streamId={}", frame.getStreamId(), e);
            throw e;
        }
    }
}
