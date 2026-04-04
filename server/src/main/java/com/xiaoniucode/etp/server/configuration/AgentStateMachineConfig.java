package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.agent.action.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务端代理状态机配置
 * 负责配置服务端代理的状态机，管理代理的各种状态转换
 */
@Configuration
public class AgentStateMachineConfig {

    /**
     * 认证动作
     */
    @Autowired
    private AuthAction authAction;

    /**
     * 代理初始化动作
     */
    @Autowired
    private ProxyInitAction proxyInitAction;

    /**
     * 代理创建动作
     */
    @Autowired
    private ProxyCreateAction proxyCreateAction;

    /**
     * 创建隧道动作
     */
    @Autowired
    private CreateConnAction createConnAction;

    /**
     * GoAway 动作
     */
    @Autowired
    private GoawayAction goawayAction;

    /**
     * 认证失败动作
     */
    @Autowired
    private AuthFailureAction authFailureAction;

    /**
     * 心跳超时动作
     */
    @Autowired
    private HeartbeatTimeoutAction heartbeatTimeoutAction;

    /**
     * 重连超时动作
     */
    @Autowired
    private RetryTimeoutAction retryTimeoutAction;

    /**
     * 创建代理状态机
     * @return 代理状态机实例
     */
    @Bean("agentStateMachine")
    public StateMachine<AgentState, AgentEvent, AgentContext> createStateMachine() {
        StateMachineBuilder<AgentState, AgentEvent, AgentContext> builder = StateMachineBuilderFactory.create();

        // 新连接建立
        builder.externalTransition()
                .from(AgentState.NEW)
                .to(AgentState.AUTHENTICATING)
                .on(AgentEvent.AUTH_START)
                .when(ctx -> true)
                .perform(authAction);

        // 认证成功
        builder.externalTransition()
                .from(AgentState.AUTHENTICATING)
                .to(AgentState.CONNECTED)
                .on(AgentEvent.AUTH_SUCCESS)
                .when(ctx -> true)
                .perform(proxyInitAction);
        builder.internalTransition()
                .within(AgentState.CONNECTED)
                .on(AgentEvent.REBUILD_CONTEXT)
                .when(ctx -> true)
                .perform(proxyInitAction);
        // 认证失败
        builder.externalTransition()
                .from(AgentState.AUTHENTICATING)
                .to(AgentState.FAILED)
                .on(AgentEvent.AUTH_FAILURE)
                .when(ctx -> true)
                .perform(authFailureAction);

        // 处理代理创建请求
        builder.internalTransition()
                .within(AgentState.CONNECTED)
                .on(AgentEvent.PROXY_CREATE_REQUEST)
                .when(ctx -> true)
                .perform(proxyCreateAction);

        // 创建隧道
        builder.internalTransition()
                .within(AgentState.CONNECTED)
                .on(AgentEvent.CREATE_TUNNEL)
                .when(ctx -> true)
                .perform(createConnAction);

        // 网络断开
        builder.externalTransition()
                .from(AgentState.CONNECTED)
                .to(AgentState.DISCONNECTED)
                .on(AgentEvent.DISCONNECT)
                .when(ctx -> true)
                .perform(goawayAction);

        // 心跳超时
        builder.externalTransition()
                .from(AgentState.CONNECTED)
                .to(AgentState.DISCONNECTED)
                .on(AgentEvent.HEARTBEAT_TIMEOUT)
                .when(ctx -> true)
                .perform(heartbeatTimeoutAction);

        // 认证中断开
        builder.externalTransition()
                .from(AgentState.AUTHENTICATING)
                .to(AgentState.DISCONNECTED)
                .on(AgentEvent.DISCONNECT)
                .when(ctx -> true)
                .perform(goawayAction);

        // 收到 GoAway 指令
        builder.externalTransitions()
                .fromAmong(AgentState.NEW,
                        AgentState.AUTHENTICATING,
                        AgentState.CONNECTED,
                        AgentState.DISCONNECTED)
                .to(AgentState.CLOSED)
                .on(AgentEvent.GOAWAY)
                .when(ctx -> true)
                .perform(goawayAction);

        // 重连窗口超时
        builder.externalTransition()
                .from(AgentState.DISCONNECTED)
                .to(AgentState.FAILED)
                .on(AgentEvent.RETRY_TIMEOUT)
                .when(ctx -> true)
                .perform(retryTimeoutAction);
        // 重连
        builder.externalTransition()
                .from(AgentState.DISCONNECTED)
                .to(AgentState.AUTHENTICATING)
                .on(AgentEvent.RETRY_CONNECT)
                .when(ctx -> true)
                .perform(authAction);
        return builder.build("agent-state-machine");
    }
}