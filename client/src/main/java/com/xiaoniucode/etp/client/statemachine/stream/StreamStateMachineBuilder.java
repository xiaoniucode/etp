package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.client.statemachine.stream.action.StreamCloseAction;
import com.xiaoniucode.etp.client.statemachine.stream.action.StreamOpenAction;

/**
 * 客户端流状态机构建器
 * 负责构建和配置客户端流的状态机，管理流的各种状态转换
 */
public class StreamStateMachineBuilder {

    /**
     * 状态机 ID
     */
    private static final String MACHINE_ID = "streamStateMachine";

    /**
     * 状态机持有者
     */
    private static class StateMachineHolder {
        /**
         * 状态机实例
         */
        private static final StateMachine<StreamState, StreamEvent, StreamContext> INSTANCE = build();

        /**
         * 构建状态机
         * @return 构建好的状态机实例
         */
        private static StateMachine<StreamState, StreamEvent, StreamContext> build() {
            StateMachineBuilder<StreamState, StreamEvent, StreamContext> builder = 
                    StateMachineBuilderFactory.create();

            // 打开流
            builder.externalTransition()
                    .from(StreamState.IDLE)
                    .to(StreamState.OPENING)
                    .on(StreamEvent.STREAM_OPEN)
                    .when(ctx -> true)
                    .perform(new StreamOpenAction());

            // 打开流成功
            builder.externalTransition()
                    .from(StreamState.OPENING)
                    .to(StreamState.OPENED)
                    .on(StreamEvent.STREAM_OPEN_SUCCESS)
                    .when(ctx -> true)
                    .perform((from, to, event, context) -> context.setState(to));

            // 关闭流
            builder.externalTransitions()
                    .fromAmong(StreamState.OPENED, StreamState.OPENING, StreamState.FAILED)
                    .to(StreamState.CLOSED)
                    .on(StreamEvent.STREAM_CLOSE)
                    .when(ctx -> true)
                    .perform(new StreamCloseAction());

            builder.build(MACHINE_ID);
            return StateMachineFactory.get(MACHINE_ID);
        }
    }

    /**
     * 获取状态机实例
     * @return 状态机实例
     */
    public static StateMachine<StreamState, StreamEvent, StreamContext> getStateMachine() {
        return StateMachineHolder.INSTANCE;
    }
}