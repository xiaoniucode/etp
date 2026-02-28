package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamState;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.action.CheckTargetAction;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.action.StreamOpenAction;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.action.StreamOpenResponseAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamStateMachineFactory {
    @Autowired
    private CheckTargetAction checkTargetAction;
    @Autowired
    private StreamOpenAction streamOpenAction;
    @Autowired
    private StreamOpenResponseAction streamOpenResponseAction;

    public StateMachine<ClientStreamState, ClientStreamEvent, StreamContext> create(int streamId) {
        StateMachineBuilder<ClientStreamState, ClientStreamEvent, StreamContext> builder = StateMachineBuilderFactory.create();

        builder.externalTransition()
                .from(ClientStreamState.INITIALIZED)
                .to(ClientStreamState.CHECKING_TARGET)
                .on(ClientStreamEvent.STREAM_OPEN)
                .when(ctx -> true)
                .perform(checkTargetAction);


        builder.externalTransition()
                .from(ClientStreamState.CHECKING_TARGET)
                .to(ClientStreamState.OPENING)
                .on(ClientStreamEvent.TARGET_VALIDATED)
                .when(ctx -> ctx.getControl() != null && ctx.getProxyConfig() != null)
                .perform(streamOpenAction);

        builder.externalTransition()
                .from(ClientStreamState.OPENING)
                .to(ClientStreamState.OPENED)
                .on(ClientStreamEvent.STREAM_OPEN_SUCCESS)
                .when(ctx -> true)
                .perform(streamOpenResponseAction);

        builder.externalTransition()
                .from(ClientStreamState.OPENED)
                .to(ClientStreamState.OPENED)
                .on(ClientStreamEvent.STREAM_DATA)
                .when(ctx -> true)
                .perform((from, to, event, context) -> context.setState(to));
//
//        // 检查目标 -> 打开中
//        builder.externalTransition()
//                .from(ClientStreamState.OPENING)
//                .to(ClientStreamState.OPENED)
//                .on(ClientStreamEvent.STREAM_OPEN_SUCCESS)
//                .when(ctx -> true)
//        ;
//        // 打开中 -> 失败
//        builder.externalTransition()
//                .from(ClientStreamState.OPENING)
//                .to(ClientStreamState.FAILED)
//                .on(ClientStreamEvent.STREAM_OPEN_FAILURE)
//                .when(ctx -> true)
//        ;
//
//
//        // 打开 -> 失败
//        builder.externalTransition()
//                .from(ClientStreamState.OPENING)
//                .to(ClientStreamState.FAILED)
//                .on(ClientStreamEvent.STREAM_OPEN_FAILURE)
//                .when(ctx -> true)
//        ;
//
//        // 已打开 -> 关闭
//        builder.externalTransition()
//                .from(ClientStreamState.OPENED)
//                .to(ClientStreamState.CLOSED)
//                .on(ClientStreamEvent.STREAM_CLOSE)
//                .when(ctx -> true)
//        ;
//
//        // 已打开 -> 关闭
//        builder.externalTransition()
//                .from(ClientStreamState.FAILED)
//                .to(ClientStreamState.CLOSED)
//                .on(ClientStreamEvent.STREAM_RESET)
//                .when(ctx -> true)
//        ;
//
//        // 已打开 -> 失败
//        builder.externalTransition()
//                .from(ClientStreamState.OPENED)
//                .to(ClientStreamState.FAILED)
//                .on(ClientStreamEvent.STREAM_RESET)
//                .when(ctx -> true)
//        ;
//


        return builder.build("streamStateMachine:" + streamId);
    }
}
