package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
import io.netty.channel.Channel;

public class DirectBridgeFactory {

    public static void bridge(StreamContext streamContext, Channel visitor, Channel tunnel, BandwidthLimiter limiter) {
        // 创建基础桥接器
        BaseChannelBridge visitorToTunnelBridge = new BaseChannelBridge(
                streamContext, tunnel, "visitor->tunnel"
        );
        BaseChannelBridge tunnelToVisitorBridge = new BaseChannelBridge(
                streamContext, visitor, "tunnel->visitor"
        );

        // 添加限流装饰器
        if (limiter != null) {
            visitorToTunnelBridge = new UploadBandwidthLimitDecorator(visitorToTunnelBridge, limiter);
            tunnelToVisitorBridge = new DownloadBandwidthLimitDecorator(tunnelToVisitorBridge, limiter);
        }

        // 添加到 pipeline
        visitor.pipeline().addLast(visitorToTunnelBridge);
        tunnel.pipeline().addLast(tunnelToVisitorBridge);
    }

    public static void bridge(StreamContext streamContext, Channel visitor, Channel tunnel) {
        bridge(streamContext, visitor, tunnel, null);
    }
}