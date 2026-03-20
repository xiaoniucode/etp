package com.xiaoniucode.etp.server.transport.bridge;

import io.netty.buffer.ByteBuf;

public interface TunnelBridge {
    void open();

    void relayToTunnel(ByteBuf payload);

    void relayToVisitor(ByteBuf payload);
}