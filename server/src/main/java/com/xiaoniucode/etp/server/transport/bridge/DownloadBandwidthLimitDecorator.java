package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadBandwidthLimitDecorator extends ChannelBridgeDecorator {
    private static final Logger logger = LoggerFactory.getLogger(DownloadBandwidthLimitDecorator.class);
    private final BandwidthLimiter limiter;

    public DownloadBandwidthLimitDecorator(ChannelBridge delegate, BandwidthLimiter limiter) {
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

        if (!limiter.tryDownload(buf)) {
            logger.debug("内网 -> 公网 下载流量限速");
            if (target.isActive()) {
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            }
            return false;
        }
        return true;
    }
}