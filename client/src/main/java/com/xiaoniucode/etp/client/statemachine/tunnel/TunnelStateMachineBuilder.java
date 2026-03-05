package com.xiaoniucode.etp.client.statemachine.tunnel;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.client.statemachine.tunnel.action.TunnelCloseAction;
import com.xiaoniucode.etp.client.statemachine.tunnel.action.TunnelCreateRespAction;

public class TunnelStateMachineBuilder {

    private static final String MACHINE_ID = "tunnelStateMachine";

    private static class StateMachineHolder {
        private static final StateMachine<TunnelState, TunnelEvent, TunnelContext> INSTANCE = build();

        private static StateMachine<TunnelState, TunnelEvent, TunnelContext> build() {
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
                    .perform((from, to, event, context) ->
                            context.setState(to));

            builder.externalTransitions()
                    .fromAmong(TunnelState.ESTABLISHED, TunnelState.CREATING)
                    .to(TunnelState.CLOSED)
                    .on(TunnelEvent.CLOSE)
                    .when(ctx -> true)
                    .perform(new TunnelCloseAction());
            builder.build(MACHINE_ID);
            return StateMachineFactory.get(MACHINE_ID);
        }
    }

    public static StateMachine<TunnelState, TunnelEvent, TunnelContext> getStateMachine() {
        return StateMachineHolder.INSTANCE;
    }
}