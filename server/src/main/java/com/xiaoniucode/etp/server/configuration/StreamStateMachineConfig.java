package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.action.CheckTargetAction;
import com.xiaoniucode.etp.server.statemachine.stream.action.StreamCloseAction;
import com.xiaoniucode.etp.server.statemachine.stream.action.StreamOpenAction;
import com.xiaoniucode.etp.server.statemachine.stream.action.StreamOpenResponseAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class StreamStateMachineConfig {
    @Autowired
    private CheckTargetAction checkTargetAction;
    @Autowired
    private StreamOpenAction streamOpenAction;
    @Autowired
    private StreamOpenResponseAction streamOpenResponseAction;
    @Autowired
    private StreamCloseAction streamCloseAction;

    @Bean("streamStateMachine")
    public StateMachine<StreamState, StreamEvent, StreamContext> create() {
        StateMachineBuilder<StreamState, StreamEvent, StreamContext> builder = StateMachineBuilderFactory.create();
        //初始化 --> 检查访问目标
        builder.externalTransition()
                .from(StreamState.IDLE)
                .to(StreamState.CHECKING_TARGET)
                .on(StreamEvent.STREAM_OPEN)
                .when(ctx -> true)
                .perform(checkTargetAction);

        // 检查访问目标 --> 连接中 校验完成
        builder.externalTransition()
                .from(StreamState.CHECKING_TARGET)
                .to(StreamState.OPENING)
                .on(StreamEvent.TARGET_VALIDATED)
                .when(ctx -> ctx.getControl() != null && ctx.getProxyConfig() != null)
                .perform(streamOpenAction);
        //连接中 --> 已连接 连接成功
        builder.externalTransition()
                .from(StreamState.OPENING)
                .to(StreamState.OPENED)
                .on(StreamEvent.STREAM_OPEN_SUCCESS)
                .when(ctx -> true)
                .perform(streamOpenResponseAction);

        builder.externalTransition()
                .from(StreamState.OPENED)
                .to(StreamState.OPENED)
                .on(StreamEvent.STREAM_DATA)
                .when(ctx -> true)
                .perform((from, to, event, context) ->
                        context.setState(to));

        builder.externalTransition()
                .from(StreamState.OPENED)
                .to(StreamState.CLOSED)
                .on(StreamEvent.STREAM_CLOSE)
                .when(ctx -> true)
                .perform(streamCloseAction);

        return builder.build("stream-state-machine");
    }
}
