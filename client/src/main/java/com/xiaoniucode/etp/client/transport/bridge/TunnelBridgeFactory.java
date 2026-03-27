package com.xiaoniucode.etp.client.transport.bridge;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.core.transport.TlsContextHolder;
import com.xiaoniucode.etp.core.transport.EncryptTunnelBridgeDecorator;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import io.netty.handler.ssl.SslContext;

import java.util.Optional;

public class TunnelBridgeFactory {

    public static TunnelBridge buildDirect(StreamContext ctx) {
        final TunnelBridge base = new DirectTunnelBridge(ctx);
        TunnelBridge bridge = base;
        Optional<SslContext> sslContext = TlsContextHolder.get();
        if (sslContext.isPresent()) {
            bridge = new EncryptTunnelBridgeDecorator(bridge, sslContext.get(), ctx);
        }
        return bridge;
    }

    public static TunnelBridge buildMux(StreamContext ctx) {
        final TunnelBridge base = new MultiplexTunnelBridge(ctx);
        TunnelBridge bridge = base;
        Optional<SslContext> sslContext = TlsContextHolder.get();
        if (sslContext.isPresent()) {
            bridge = new EncryptTunnelBridgeDecorator(bridge, sslContext.get(), ctx);
        }
        return bridge;
    }
}