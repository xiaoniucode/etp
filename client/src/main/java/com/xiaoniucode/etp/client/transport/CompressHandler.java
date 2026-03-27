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

package com.xiaoniucode.etp.client.transport;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.client.statemachine.stream.StreamState;
import com.xiaoniucode.etp.core.transport.compress.CompressionType;
import com.xiaoniucode.etp.core.transport.compress.Compressor;
import com.xiaoniucode.etp.core.transport.compress.CompressorFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


import java.util.Optional;

@ChannelHandler.Sharable
public class CompressHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(CompressHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Channel server = ctx.channel();
        Optional<StreamContext> streamContextOpt = StreamManager.getStreamContext(server);
        if (streamContextOpt.isEmpty()){
            ctx.fireChannelRead(msg.retain());
            return;
        }

        StreamContext streamContext = streamContextOpt.get();
        if (!streamContext.isCompress()) {
            logger.debug("不支持压缩");
            ctx.fireChannelRead(msg.retain());
            return;
        }
        if (streamContext.getState()!= StreamState.OPENED){
            logger.debug("流尚未打开，不压缩");
            ctx.fireChannelRead(msg.retain());
            return;
        }
        Compressor compressor = CompressorFactory.getCompressor(CompressionType.DEFAULT);
        compressor.compress(server, msg, compressed -> {
            try {
                ctx.fireChannelRead(compressed);
            } catch (Exception e) {
                ctx.fireExceptionCaught(e);
            }
        }, 4);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }
}
