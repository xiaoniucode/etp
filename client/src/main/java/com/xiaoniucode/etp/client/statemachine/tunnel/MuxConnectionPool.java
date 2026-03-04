package com.xiaoniucode.etp.client.statemachine.tunnel;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MuxConnectionPool {
    private final static Map<ProtocolFeature, TunnelContext> caches = new ConcurrentHashMap<>();

    public static Optional<TunnelContext> acquire(ProtocolFeature feature) {
        TunnelContext tunnelContext = caches.get(feature);
        if (tunnelContext == null) {
            return Optional.empty();
        }
        if (!tunnelContext.getTunnel().isActive()) {
            tunnelContext.fireEvent(TunnelEvent.CLOSE);
        }
        return Optional.ofNullable(tunnelContext);
    }

    public static void add(ProtocolFeature feature, TunnelContext context) {
        caches.put(feature, context);
    }
}
