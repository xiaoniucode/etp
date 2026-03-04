package com.xiaoniucode.etp.client.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.common.UUIDGenerator;
import com.xiaoniucode.etp.client.statemachine.stream.TunnelConfig;
import com.xiaoniucode.etp.core.statemachine.TunnelType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TunnelManager {
    private static final Map<String, TunnelContext> tunnels = new ConcurrentHashMap<>();

    public static TunnelContext createTunnelContext(int connectionId) {
        String tunnelId = UUIDGenerator.generate();

        StateMachine<TunnelState, TunnelEvent, TunnelContext> stateMachine = TunnelStateMachineBuilder.getStateMachine();
        TunnelContext tunnelContext = new TunnelContext();
        tunnelContext.setStateMachine(stateMachine);
        tunnelContext.setTunnelId(tunnelId);
        tunnelContext.setConnectionId(connectionId);
        tunnels.put(tunnelId, tunnelContext);
        DirectConnectionPool.add(tunnelContext);
        return tunnelContext;
    }

    public static Optional<TunnelContext> getTunnelContext(String tunnelId) {
        return Optional.ofNullable(tunnels.get(tunnelId));
    }

    public static Optional<TunnelContext> acquire(TunnelConfig tunnelConfig) {
        if (tunnelConfig.isMux()) {
            ProtocolFeature feature;
            if (tunnelConfig.isEncrypt() && tunnelConfig.isCompress()) {
                feature = ProtocolFeature.ENCRYPT_COMPRESS;
            } else if (tunnelConfig.isEncrypt()) {
                feature = ProtocolFeature.ENCRYPT;
            } else if (tunnelConfig.isCompress()) {
                feature = ProtocolFeature.COMPRESS;
            } else {
                feature = ProtocolFeature.PLAIN;
            }
            return MuxConnectionPool.acquire(feature);
        } else {
            return DirectConnectionPool.acquire();
        }
    }

    public static void release(TunnelContext tunnelContext) {
        if (tunnelContext.getTunnelType()== TunnelType.DIRECT){
            DirectConnectionPool.release(tunnelContext);
        }
    }

    public static void remove(TunnelContext context) {
        if (context.getTunnelType()== TunnelType.DIRECT){
            DirectConnectionPool.remove(context);
        }
    }
}
