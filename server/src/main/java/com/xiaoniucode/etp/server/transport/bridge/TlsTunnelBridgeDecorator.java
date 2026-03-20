package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.core.netty.TlsHandlerCleanup;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.transport.TlsContextHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

public class TlsTunnelBridgeDecorator extends AbstractTunnelBridgeDecorator {
    public TlsTunnelBridgeDecorator(TunnelBridge delegate, StreamContext streamContext) {
        super(delegate, streamContext);
    }

    @Override
    public void open() {
        Channel tunnel = streamContext.getTunnel();
        if (tunnel == null) {
            delegate.open();
            return;
        }
        ChannelPipeline pipeline = tunnel.pipeline();
        boolean encrypt = streamContext.isEncrypt();

        if (!encrypt) {
            if (pipeline.get(NettyConstants.TLS_HANDLER) != null) {
                TlsHandlerCleanup.removeTlsGracefully(pipeline);
            }
        } else {
            TlsContextHolder.get().ifPresent(sslContext -> {
                SslHandler sslHandler = sslContext.newHandler(tunnel.alloc());
                if (pipeline.get(NettyConstants.TLS_HANDLER) == null) {
                    pipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
                } else {
                    pipeline.replace(NettyConstants.TLS_HANDLER, NettyConstants.TLS_HANDLER, sslHandler);
                }
            });
        }

      //  delegate.open();
    }
}