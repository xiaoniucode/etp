package com.xiaoniucode.etp.core.transport;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

/**
 * 检测是加密还是明文连接，动态处理SSL握手
 */
public class EtpOptionalSslHandler extends OptionalSslHandler {

    public EtpOptionalSslHandler(SslContext sslContext) {
        super(sslContext);
    }

    @Override
    protected String newSslHandlerName() {
        return NettyConstants.TLS_HANDLER;
    }

    @Override
    protected String newNonSslHandlerName() {
        return null;
    }

    @Override
    protected ChannelHandler newNonSslHandler(ChannelHandlerContext context) {
        return null;
    }
}