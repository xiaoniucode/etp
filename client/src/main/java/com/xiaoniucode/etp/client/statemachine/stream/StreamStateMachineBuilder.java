package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.client.statemachine.stream.action.StreamOpenAction;

public class StreamStateMachineBuilder {

    public static StateMachine<StreamState, StreamEvent, StreamContext> buildStateMachine(Integer streamId) {
        StateMachineBuilder<StreamState, StreamEvent, StreamContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition()
                .from(StreamState.INITIALIZED)
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

        // 构建状态机
        String machineId = "streamStateMachine:"+streamId;
        builder.build(machineId);
        return StateMachineFactory.get(machineId);
    }
}