package com.xiaoniucode.etp.client.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;

public class TunnelStateMachineBuilder {

    public static StateMachine<TunnelState, TunnelEvent, TunnelContext> buildStateMachine(Integer id) {
        StateMachineBuilder<TunnelState, TunnelEvent, TunnelContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition()
                .from(TunnelState.IDLE)
                .to(TunnelState.CREATING)
                .on(TunnelEvent.CONNECT)
                .when(ctx -> true)
                .perform((from, to, event, context) -> context.setState(to));


        builder.externalTransition()
                .from(TunnelState.CREATING)
                .to(TunnelState.CREATING)
                .on(TunnelEvent.CREATE_RESPONSE)
                .when(ctx -> true)
                .perform(new TunnelCreateRespAction());


        builder.externalTransition()
                .from(TunnelState.CREATING)
                .to(TunnelState.ESTABLISHED)
                .on(TunnelEvent.CREATE_SUCCESS)
                .when(ctx -> true)
                .perform((from, to, event, context) -> context.setState(to));
        String machineId = "tunnelStateMachine:" + id;
        builder.build(machineId);
        return StateMachineFactory.get(machineId);
    }
}