package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import com.xiaoniucode.etp.server.utils.NettyHttpUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadBandwidthLimitDecorator extends ChannelBridgeDecorator {
    private static final Logger logger = LoggerFactory.getLogger(UploadBandwidthLimitDecorator.class);
    private final BandwidthLimiter limiter;

    public UploadBandwidthLimitDecorator(ChannelBridge delegate, BandwidthLimiter limiter) {
        super(delegate);
        this.limiter = limiter;
    }

    @Override
    protected boolean beforeChannelRead(ChannelHandlerContext ctx, Object msg) {
        if (limiter == null) {
            return true;
        }
        if (!(msg instanceof ByteBuf buf)) {
            return true;
        }

        if (!limiter.tryUpload(buf)) {
            logger.debug("[HTTP] 上传数据限流");
            ProtocolType protocol = streamContext.getCurrentProtocol();
            if (protocol.isHttp()) {
                NettyHttpUtils.sendHttpTooManyRequests(ctx.channel())
                        .addListener(f ->
                                streamContext.fireEvent(StreamEvent.STREAM_CLOSE));
            }
            return false;
        }
        return true;
    }
}