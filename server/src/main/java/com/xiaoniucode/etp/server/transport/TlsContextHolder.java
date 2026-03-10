package com.xiaoniucode.etp.server.transport;

import io.netty.handler.ssl.SslContext;

import java.util.Optional;

public final class TlsContextHolder {
    private static SslContext tlsContext;

    private TlsContextHolder() {
    }

    public static void initialize(SslContext context) {
        if (context == null) {
            throw new IllegalArgumentException("TLS context cannot be null");
        }
        if (tlsContext != null) {
            throw new IllegalStateException("TLS context already initialized");
        }
        tlsContext = context;
    }

    public static Optional<SslContext> get() {
        return Optional.ofNullable(tlsContext);
    }
}
