package com.xiaoniucode.etp.client.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TunnelManager {
    private final static AtomicInteger auto = new AtomicInteger(0);
    private static final Map<Integer, TunnelContext> tunnels = new ConcurrentHashMap<>();

    public static TunnelContext createTunnelContext(int connectionId) {
        int tunnelId = auto.incrementAndGet();
        StateMachine<TunnelState, TunnelEvent, TunnelContext> stateMachine = TunnelStateMachineBuilder.buildStateMachine(tunnelId);
        TunnelContext tunnelContext = new TunnelContext();
        tunnelContext.setStateMachine(stateMachine);
        tunnelContext.setTunnelId(tunnelId);
        tunnelContext.setConnectionId(connectionId);
        tunnels.put(tunnelId, tunnelContext);
        return tunnelContext;
    }

    public static Optional<TunnelContext> getTunnelContext(int requestId) {
        return Optional.ofNullable(tunnels.get(requestId));
    }
}
