package com.xiaoniucode.etp.server.statemachine.tunnel;

import org.springframework.stereotype.Component;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多路复用隧道
 */
@Component
public class MuxTunnelManager {
    private final Map<ProtocolFeature, TunnelContext> tunnels = new ConcurrentHashMap<>();

    public TunnelContext add(ProtocolFeature feature, TunnelContext context) {
        TunnelContext tunnelContext = tunnels.putIfAbsent(feature, context);
        return tunnelContext;
    }

    public Optional<TunnelContext> get(ProtocolFeature feature) {
        TunnelContext tunnelContext = tunnels.get(feature);
        return Optional.ofNullable(tunnelContext);
    }
    public void remove(){

    }

    public TunnelContext register(TunnelContext context) {
        return context;
    }
}
