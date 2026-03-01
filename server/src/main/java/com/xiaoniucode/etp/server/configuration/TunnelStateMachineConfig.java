package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelState;
import com.xiaoniucode.etp.server.statemachine.tunnel.action.CreateTunnelAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TunnelStateMachineConfig {

    @Autowired
    private CreateTunnelAction createTunnelAction;

    @Bean("tunnelStateMachine")
    public StateMachine<TunnelState, TunnelEvent, TunnelContext> createStateMachine() {
        StateMachineBuilder<TunnelState, TunnelEvent, TunnelContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition()
                .from(TunnelState.IDLE)
                .to(TunnelState.CREATING)
                .on(TunnelEvent.CREATE)
                .when(ctx -> true)
                .perform(createTunnelAction);

        builder.externalTransition()
                .from(TunnelState.CREATING)
                .to(TunnelState.ESTABLISHED)
                .on(TunnelEvent.CREATE_SUCCESS)
                .when(ctx -> true)
                .perform((from, to, event, context) -> context.setState(to));

        return builder.build("tunnel-state-machine");
    }
}