package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.buffer.ByteBuf;

public abstract class AbstractTunnelBridgeDecorator implements TunnelBridge {
    protected final TunnelBridge delegate;
    protected final StreamContext streamContext;

    protected AbstractTunnelBridgeDecorator(TunnelBridge delegate, StreamContext streamContext) {
        this.delegate = delegate;
        this.streamContext = streamContext;
    }

    @Override
    public void open() {
        delegate.open();
    }

    @Override
    public void relayToTunnel(ByteBuf payload) {
        delegate.relayToTunnel(payload);
    }

    @Override
    public void relayToVisitor(ByteBuf payload) {
        delegate.relayToVisitor(payload);
    }
}