/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.core.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import io.netty.handler.ssl.SslContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


public class EncryptTunnelBridgeDecorator extends AbstractTunnelBridgeDecorator {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(EncryptTunnelBridgeDecorator.class);
    private final AbstractStreamContext streamContext;
    private final SslContext sslContext;

    public EncryptTunnelBridgeDecorator(TunnelBridge delegate, SslContext sslContext, AbstractStreamContext streamContext) {
        super(delegate);
        this.sslContext = sslContext;
        this.streamContext = streamContext;
    }

    @Override
    public void open() {
        if (sslContext == null) {
            throw new IllegalArgumentException("SSL context is required for encrypt tunnel bridge decorator");
        }
        TunnelEntry tunnelEntry = streamContext.getTunnelEntry();
        if (tunnelEntry == null) {
            logger.warn("数据连接为空：streamId={}",streamContext.getStreamId());
            delegate.open();
            return;
        }
        Channel tunnel = tunnelEntry.getChannel();
        ChannelPipeline pipeline = tunnel.pipeline();
        boolean encrypt = streamContext.isEncrypt();

        if (!encrypt) {
            logger.debug("Removing TLS handler from pipeline for streamId={}", streamContext.getStreamId());
            if (pipeline.get(NettyConstants.TLS_HANDLER) != null) {
                TlsHandlerCleanup.removeTlsGracefully(pipeline);
            }
        } else {
            logger.debug("Adding TLS handler to pipeline for streamId={}", streamContext.getStreamId());
            SslHandler sslHandler = sslContext.newHandler(tunnel.alloc());
            if (pipeline.get(NettyConstants.TLS_HANDLER) == null) {
                pipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
            } else {
                //pipeline.replace(NettyConstants.TLS_HANDLER, NettyConstants.TLS_HANDLER, sslHandler);
            }
        }
        delegate.open();
    }
}