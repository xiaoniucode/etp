package com.xiaoniucode.etp.client.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.client.statemachine.agent.action.*;

public class AgentStateMachineBuilder {

    public static StateMachine<ClientState, ClientEvent, AgentContext> buildStateMachine() {
        StateMachineBuilder<ClientState, ClientEvent, AgentContext> builder = StateMachineBuilderFactory.create();

        // 创建动作实例
        CheckConfigAction checkConfigAction = new CheckConfigAction();
        InitSslAction initSslAction = new InitSslAction();
        ConnectAction connectAction = new ConnectAction();
        HandleAuthSuccessAction handleAuthSuccessAction = new HandleAuthSuccessAction();
        HandleAuthFailureAction handleAuthFailureAction = new HandleAuthFailureAction();
        HandleConnectFailureAction handleConnectFailureAction = new HandleConnectFailureAction();
        AuthAction handleConnectSuccessAction = new AuthAction();
        StopAction stopAction = new StopAction();
        // 配置检查
        builder.externalTransition()
                .from(ClientState.INITIALIZED)
                .to(ClientState.CONFIG_CHECKING)
                .on(ClientEvent.START)
                .when(ctx -> true)
                .perform(checkConfigAction);

        // SSL初始化
        builder.externalTransition()
                .from(ClientState.CONFIG_CHECKING)
                .to(ClientState.SSL_INITIALIZING)
                .on(ClientEvent.CONFIG_CHECKED)
                .when(AgentContext::isConfigValid)
                .perform(initSslAction);

        // 连接尝试
        builder.externalTransition()
                .from(ClientState.SSL_INITIALIZING)
                .to(ClientState.CONNECTING)
                .on(ClientEvent.SSL_INITIALIZED)
                .when(context -> true)
                .perform(connectAction);

        // 从CONNECTED状态发生网络错误 → FAILED
        builder.externalTransition()
                .from(ClientState.CONNECTED)
                .to(ClientState.FAILED)
                .on(ClientEvent.NETWORK_ERROR)
                .when(ctx -> true)
                .perform(new NetworkErrorAction());


        // 连接成功后开始认证
        builder.externalTransition()
                .from(ClientState.CONNECTING)
                .to(ClientState.AUTHENTICATING)
                .on(ClientEvent.CONNECT_SUCCESS)
                .when(ctx -> true)
                .perform(handleConnectSuccessAction);

        // 认证成功
        builder.externalTransition()
                .from(ClientState.AUTHENTICATING)
                .to(ClientState.CONNECTED)
                .on(ClientEvent.AUTH_SUCCESS)
                .when(AgentContext::isAuthenticated)
                .perform(handleAuthSuccessAction);

        // 认证失败
        builder.externalTransition()
                .from(ClientState.AUTHENTICATING)
                .to(ClientState.FAILED)
                .on(ClientEvent.AUTH_FAILURE)
                .when(ctx -> !ctx.isAuthenticated())
                .perform(handleAuthFailureAction);
        // 连接失败
        builder.externalTransition()
                .from(ClientState.CONNECTING)
                .to(ClientState.FAILED)
                .on(ClientEvent.CONNECT_FAILURE)
                .when(ctx -> true)
                .perform(handleConnectFailureAction);

        // 重试
        builder.externalTransition()
                .from(ClientState.FAILED)
                .to(ClientState.CONNECTING)
                .on(ClientEvent.RETRY)
                .when(ctx -> true)
                .perform(new RetryAction());

        // 停止
        builder.externalTransition()
                .from(ClientState.FAILED)
                .to(ClientState.STOPPED)
                .on(ClientEvent.STOP)
                .when(ctx -> true)
                .perform(stopAction);


        builder.externalTransition()
                .from(ClientState.CONNECTED)
                .to(ClientState.CONNECTED)
                .on(ClientEvent.PROXY_CREATE_RESP)
                .when(ctx -> true)
                .perform(new ProxyCreateResponseAction());


        // 构建状态机
        String machineId = "clientStateMachine";
        builder.build(machineId);
        return StateMachineFactory.get(machineId);
    }
}