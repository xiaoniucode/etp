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

package com.xiaoniucode.etp.server.transport;

import com.xiaoniucode.etp.core.transport.compress.CompressionType;
import com.xiaoniucode.etp.core.transport.compress.Compressor;
import com.xiaoniucode.etp.core.transport.compress.CompressorFactory;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ChannelHandler.Sharable
public class CompressHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(CompressHandler.class);
    @Autowired
    private StreamManager streamManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Channel visitor = ctx.channel();
        Optional<StreamContext> contextOpt = streamManager.getStreamContext(visitor);

        if (contextOpt.isEmpty()) {
            ctx.fireChannelRead(msg.retain());
            logger.debug("引用计数为：{}",msg.refCnt());
            return;
        }

        StreamContext streamContext = contextOpt.get();

        if (!streamContext.isCompress()) {
            ctx.fireChannelRead(msg.retain());
            logger.debug("不支持压缩，直接返回，引用计数: {}",msg.refCnt());
            return;
        }
        if (streamContext.getState()!= StreamState.OPENED){
            logger.debug("流尚未打开，不压缩");
            ctx.fireChannelRead(msg.retain());
            return;
        }
        logger.debug("压缩数据包：streamId={}", streamContext.getStreamId());
        Compressor compressor = CompressorFactory.getCompressor(CompressionType.DEFAULT);
        compressor.compress(visitor, msg, compressed -> {
            try {
                logger.debug("streamId={} 压缩块",streamContext.getStreamId());
                ctx.fireChannelRead(compressed);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.fireExceptionCaught(e);
            }
        }, 4);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }
}
