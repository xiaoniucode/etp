package com.xiaoniucode.etp.client.transport.bridge;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.core.transport.TunnelBridge;

public class TunnelBridgeFactory {

    public static TunnelBridge buildDirect(StreamContext ctx) {
        final TunnelBridge base = new DirectTunnelBridge(ctx);
        TunnelBridge bridge = base;
        return bridge;
    }

    public static TunnelBridge buildMux(StreamContext ctx) {
        final TunnelBridge base = new MultiplexTunnelBridge(ctx);
        TunnelBridge bridge = base;
        return bridge;
    }
}