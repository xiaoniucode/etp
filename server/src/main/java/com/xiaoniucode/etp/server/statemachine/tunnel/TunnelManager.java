package com.xiaoniucode.etp.server.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TunnelManager {
    @Autowired
    private DirectTunnelPoolManager directTunnelPoolManager;
    @Autowired
    private MuxTunnelManager muxTunnelManager;
    private Map<String, TunnelContext> contexts = new ConcurrentHashMap<>();
    @Autowired
    @Qualifier("tunnelStateMachine")
    private StateMachine<TunnelState, TunnelEvent, TunnelContext> tunnelStateMachine;

    public Optional<TunnelContext> getTunnel(boolean mux, String tunnelId) {
        if (mux) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(directTunnelPoolManager.borrow(tunnelId));
        }
    }

    public TunnelContext createContext(AgentContext agentContext, String tunnelId, Channel tunnel, boolean isMuxTunnel) {
        TunnelContext context = TunnelContext.builder()
                .connectionId(agentContext.getConnectionId())
                .control(agentContext.getControl())
                .isMux(isMuxTunnel)
                .tunnel(tunnel)
                .stateMachine(tunnelStateMachine)
                .tunnelId(tunnelId)
                .build();
        context.setState(TunnelState.IDLE);
        if (isMuxTunnel) {
            return muxTunnelManager.register(context);
        } else {
            return directTunnelPoolManager.register(context);
        }
    }
}
