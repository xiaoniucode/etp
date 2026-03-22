package com.xiaoniucode.etp.server.statemachine.tunnel;

import com.xiaoniucode.etp.core.transport.ProtocolFeature;
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
    private final Map<String, TunnelContext> contextMap = new ConcurrentHashMap<>();

    public TunnelContext add(TunnelContext context) {
        ProtocolFeature feature = ProtocolFeature.toProtocolFeature(context.isEncrypt(), context.isCompress());
        contextMap.put(context.getTunnelId(),context);
        return tunnels.put(feature, context);
    }

    public Optional<TunnelContext> get(String tunnelId) {
        TunnelContext tunnelContext = contextMap.get(tunnelId);
        return Optional.ofNullable(tunnelContext);
    }

    public void remove() {

    }

    public TunnelContext register(TunnelContext context) {
        return context;
    }
}
