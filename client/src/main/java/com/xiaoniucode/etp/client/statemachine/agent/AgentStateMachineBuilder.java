package com.xiaoniucode.etp.client.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.StateMachineFactory;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.client.statemachine.agent.action.*;
import com.xiaoniucode.etp.client.statemachine.agent.action.auth.AuthAction;
import com.xiaoniucode.etp.client.statemachine.agent.action.auth.HandleAuthFailureAction;
import com.xiaoniucode.etp.client.statemachine.agent.action.auth.HandleAuthSuccessAction;
import com.xiaoniucode.etp.client.statemachine.agent.action.CreateTunnelPoolAction;

public class AgentStateMachineBuilder {
    
    private static final String MACHINE_ID = "clientStateMachine";
    
    private static class StateMachineHolder {
        private static final StateMachine<AgentState, AgentEvent, AgentContext> INSTANCE = build();
        
        private static StateMachine<AgentState, AgentEvent, AgentContext> build() {
            StateMachineBuilder<AgentState, AgentEvent, AgentContext> builder = StateMachineBuilderFactory.create();

            // 创建动作实例
            CheckConfigAction checkConfigAction = new CheckConfigAction();
            InitSslAction initSslAction = new InitSslAction();
            ConnectAction connectAction = new ConnectAction();
            HandleAuthSuccessAction handleAuthSuccessAction = new HandleAuthSuccessAction();
            HandleAuthFailureAction handleAuthFailureAction = new HandleAuthFailureAction();
            HandleConnectFailureAction handleConnectFailureAction = new HandleConnectFailureAction();
            AuthAction handleConnectSuccessAction = new AuthAction();
            CreateTunnelPoolAction createTunnelPoolAction = new CreateTunnelPoolAction();
            StopAction stopAction = new StopAction();
            // 配置检查
            builder.externalTransition()
                    .from(AgentState.IDLE)
                    .to(AgentState.CONFIG_CHECKING)
                    .on(AgentEvent.START)
                    .when(ctx -> true)
                    .perform(checkConfigAction);

            // SSL初始化
            builder.externalTransition()
                    .from(AgentState.CONFIG_CHECKING)
                    .to(AgentState.SSL_INITIALIZING)
                    .on(AgentEvent.CONFIG_CHECKED)
                    .when(AgentContext::isConfigValid)
                    .perform(initSslAction);

            // 连接尝试
            builder.externalTransition()
                    .from(AgentState.SSL_INITIALIZING)
                    .to(AgentState.CONNECTING)
                    .on(AgentEvent.SSL_INITIALIZED)
                    .when(context -> true)
                    .perform(connectAction);

            // 从CONNECTED状态发生网络错误 → FAILED
            builder.externalTransition()
                    .from(AgentState.CONNECTED)
                    .to(AgentState.FAILED)
                    .on(AgentEvent.NETWORK_ERROR)
                    .when(ctx -> true)
                    .perform(new NetworkErrorAction());

            builder.externalTransitions()
                    .fromAmong(AgentState.CONNECTED)
                    .to(AgentState.DISCONNECTED)
                    .on(AgentEvent.DISCONNECT)
                    .when(ctx -> true)
                    .perform(new ClearAction());

            // 连接成功后开始认证
            builder.externalTransition()
                    .from(AgentState.CONNECTING)
                    .to(AgentState.AUTHENTICATING)
                    .on(AgentEvent.CONNECT_SUCCESS)
                    .when(ctx -> true)
                    .perform(handleConnectSuccessAction);

            // 认证成功
            builder.externalTransition()
                    .from(AgentState.AUTHENTICATING)
                    .to(AgentState.CONNECTED)
                    .on(AgentEvent.AUTH_SUCCESS)
                    .when(AgentContext::isAuthenticated)
                    .perform(handleAuthSuccessAction);
            //创建隧道池
            builder.externalTransition()
                    .from(AgentState.CONNECTED)
                    .to(AgentState.CONNECTED)
                    .on(AgentEvent.CREATE_TUNNEL_POOL)
                    .when(context -> true)
                    .perform(createTunnelPoolAction);

            // 认证失败
            builder.externalTransition()
                    .from(AgentState.AUTHENTICATING)
                    .to(AgentState.FAILED)
                    .on(AgentEvent.AUTH_FAILURE)
                    .when(ctx -> !ctx.isAuthenticated())
                    .perform(handleAuthFailureAction);
            // 连接失败
            builder.externalTransition()
                    .from(AgentState.CONNECTING)
                    .to(AgentState.FAILED)
                    .on(AgentEvent.CONNECT_FAILURE)
                    .when(ctx -> true)
                    .perform(handleConnectFailureAction);

            // 重试
            builder.externalTransition()
                    .from(AgentState.FAILED)
                    .to(AgentState.CONNECTING)
                    .on(AgentEvent.RETRY)
                    .when(ctx -> true)
                    .perform(connectAction);

            // 停止
            builder.externalTransition()
                    .from(AgentState.FAILED)
                    .to(AgentState.STOPPED)
                    .on(AgentEvent.STOP)
                    .when(ctx -> true)
                    .perform(stopAction);


            builder.externalTransition()
                    .from(AgentState.CONNECTED)
                    .to(AgentState.CONNECTED)
                    .on(AgentEvent.PROXY_CREATE_RESP)
                    .when(ctx -> true)
                    .perform(new ProxyCreateResponseAction());

            // 构建状态机
            builder.build(MACHINE_ID);
            return StateMachineFactory.get(MACHINE_ID);
        }
    }
    
    public static StateMachine<AgentState, AgentEvent, AgentContext> getStateMachine() {
        return StateMachineHolder.INSTANCE;
    }
}