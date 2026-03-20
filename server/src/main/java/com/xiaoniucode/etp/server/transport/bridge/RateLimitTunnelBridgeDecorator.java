package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import com.xiaoniucode.etp.server.utils.NettyHttpUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

public class RateLimitTunnelBridgeDecorator extends AbstractTunnelBridgeDecorator {

    public RateLimitTunnelBridgeDecorator(TunnelBridge delegate, StreamContext streamContext) {
        super(delegate, streamContext);
    }

    @Override
    public void relayToTunnel(ByteBuf payload) {
        BandwidthLimiter limiter = streamContext.getBandwidthLimiter();
        if (limiter == null) {
            delegate.relayToTunnel(payload);
            return;
        }

        if (limiter.tryUpload(payload)) {
            delegate.relayToTunnel(payload);
            return;
        }

        // 决绝：HTTP 上传时发 429 + close
        ProtocolType protocol = streamContext.getCurrentProtocol();
        Channel visitor = streamContext.getVisitor();
        try {
            if (protocol != null && protocol.isHttp()) {
                NettyHttpUtils.sendHttpTooManyRequests(visitor)
                        .addListener(f -> streamContext.fireEvent(StreamEvent.STREAM_CLOSE));
            } else {
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            }
        } finally {
            ReferenceCountUtil.release(payload);
        }
    }

    @Override
    public void relayToVisitor(ByteBuf payload) {
        BandwidthLimiter limiter = streamContext.getBandwidthLimiter();
        if (limiter == null) {
            delegate.relayToVisitor(payload);
            return;
        }

        if (limiter.tryDownload(payload)) {
            delegate.relayToVisitor(payload);
            return;
        }
        try {
            streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
        } finally {
            ReferenceCountUtil.release(payload);
        }
    }
}