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

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ChannelHandler.Sharable
public class DownloadRateLimitHandler extends SimpleChannelInboundHandler<TMSPFrame> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DownloadRateLimitHandler.class);
    @Autowired

    private StreamManager streamManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMSPFrame frame) throws Exception {
        ByteBuf payload = frame.getPayload();
        if (payload == null || !payload.isReadable()) {
            ctx.fireChannelRead(frame.retain());
            return;
        }
        int streamId = frame.getStreamId();
        //只限制数据传输
        if (frame.getMsgType() != TMSP.MSG_STREAM_DATA) {
            ctx.fireChannelRead(frame.retain());
            return;
        }
        StreamContext streamContext = streamManager.getStreamContext(streamId);
        if (streamContext == null) {
            ctx.fireChannelRead(frame.retain());
            return;
        }
//        BandwidthLimiter limiter = streamContext.getBandwidthLimiter();
//        if (limiter == null) {
//            ctx.fireChannelRead(frame.retain());
//            return;
//        }
//        TunnelEntry tunnelEntry = streamContext.getTunnelEntry();
//        Channel tunnel = tunnelEntry.getChannel();
//        Channel visitor = streamContext.getVisitor();
//        int bytes = payload.readableBytes();
//        long waitNanos = limiter.getUploadWaitNanos(bytes);
//        logger.warn("访问速度太快，触发限流：streamId={} bytes={} 等待 {} ms", streamContext.getStreamId(), bytes, waitNanos / 1_000_000);
//
//        visitor.config().setOption(ChannelOption.AUTO_READ, false);
//        tunnel.config().setOption(ChannelOption.AUTO_READ, false);
//        scheduleResume(streamContext, tunnel, visitor, waitNanos);
//        logger.debug("发送限流时从访问流收到的数据包到内网");
        ctx.fireChannelRead(frame.retain());
    }

    private void scheduleResume(StreamContext streamContext, Channel tunnel, Channel visitor, long waitNanos) {
        if (tunnel == null) return;
        long waitMillis = Math.max(1, waitNanos / 1_000_000);
        tunnel.eventLoop().schedule(() -> {
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
            visitor.config().setOption(ChannelOption.AUTO_READ, true);
            logger.debug("限流恢复，继续读取：streamId={}", streamContext.getStreamId());
            tunnel.read();
        }, waitMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("MultiplexDownloadRateLimitHandler 异常", cause);
        ctx.fireExceptionCaught(cause);
    }
}
