package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;

public class TunnelBridgeFactory {

    public static TunnelBridge buildDirect(StreamContext ctx) {
        final TunnelBridge base = new DirectTunnelBridge(ctx);
        TunnelBridge bridge = base;
//        bridge = new CompressTunnelBridgeDecorator(bridge, ctx);
//        bridge = new TlsTunnelBridgeDecorator(bridge, ctx);
        bridge = new RateLimitTunnelBridgeDecorator(bridge, ctx);
        return bridge;
    }

    public static TunnelBridge buildMux(StreamContext ctx) {
        final TunnelBridge base = new MultiplexTunnelBridge(ctx);
        TunnelBridge bridge = base;
        bridge = new RateLimitTunnelBridgeDecorator(bridge, ctx);
        return bridge;
    }
}