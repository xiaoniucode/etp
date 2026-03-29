package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.client.statemachine.stream.action.StreamCloseAction;
import com.xiaoniucode.etp.client.statemachine.stream.action.StreamOpenAction;

public class StreamStateMachineBuilder {

    private static final String MACHINE_ID = "streamStateMachine";

    private static class StateMachineHolder {
        private static final StateMachine<StreamState, StreamEvent, StreamContext> INSTANCE = build();

        private static StateMachine<StreamState, StreamEvent, StreamContext> build() {
            StateMachineBuilder<StreamState, StreamEvent, StreamContext> builder =
                    StateMachineBuilderFactory.create();

            builder.externalTransition()
                    .from(StreamState.IDLE)
                    .to(StreamState.OPENING)
                    .on(StreamEvent.STREAM_OPEN)
                    .when(ctx -> true)
                    .perform(new StreamOpenAction());

            builder.externalTransition()
                    .from(StreamState.OPENING)
                    .to(StreamState.OPENED)
                    .on(StreamEvent.STREAM_OPEN_SUCCESS)
                    .when(ctx -> true)
                    .perform((from, to, event, context) -> context.setState(to));

            builder.externalTransition()
                    .from(StreamState.OPENED)
                    .to(StreamState.OPENED)
                    .on(StreamEvent.STREAM_DATA)
                    .when(ctx -> true)
                    .perform((from, to, event, context) -> context.setState(to));

            builder.externalTransitions()
                    .fromAmong(StreamState.OPENED, StreamState.OPENING,StreamState.FAILED)
                    .to(StreamState.CLOSED)
                    .on(StreamEvent.STREAM_CLOSE)
                    .when(ctx -> true)
                    .perform(new StreamCloseAction());

            builder.build(MACHINE_ID);
            return StateMachineFactory.get(MACHINE_ID);
        }
    }

    public static StateMachine<StreamState, StreamEvent, StreamContext> getStateMachine() {
        return StateMachineHolder.INSTANCE;
    }
}