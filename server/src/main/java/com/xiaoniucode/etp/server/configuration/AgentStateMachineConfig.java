package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.action.AuthAction;
import com.xiaoniucode.etp.server.statemachine.agent.action.AgentBaseAction;
import com.xiaoniucode.etp.server.statemachine.agent.action.ProxyCreateAction;
import com.xiaoniucode.etp.server.statemachine.agent.action.ProxyInitAction;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentStateMachineConfig {

    @Autowired
    private AuthAction authAction;
    @Autowired
    private ProxyCreateAction proxyCreateAction;
    @Autowired
    private ProxyInitAction proxyInitAction;

    public StateMachine<AgentState, AgentEvent, AgentContext> createStateMachine(String machineId) {
        StateMachineBuilder<AgentState, AgentEvent, AgentContext> builder = StateMachineBuilderFactory.create();
        // 配置状态转换
        builder.externalTransition()
                .from(AgentState.INITIALIZED)
                .to(AgentState.CONNECTED)
                .on(AgentEvent.CONNECT)
                .when(ctx -> true).perform(new AgentBaseAction() {
                    @Override
                    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
                        context.setState(to);
                    }
                });

        builder.externalTransition()
                .from(AgentState.CONNECTED)
                .to(AgentState.AUTHENTICATING)
                .on(AgentEvent.AUTH_START)
                .when(ctx -> true)
                .perform(authAction);


        builder.externalTransition()
                .from(AgentState.AUTHENTICATING)
                .to(AgentState.AUTHENTICATED)
                .on(AgentEvent.AUTH_SUCCESS)
                .when(ctx -> true).perform(proxyInitAction);

        //代理 CRUD
        builder.externalTransition()
                .from(AgentState.AUTHENTICATED)
                .to(AgentState.AUTHENTICATED)
                .on(AgentEvent.PROXY_CREATE_REQUEST)
                .when(ctx -> true)
                .perform(proxyCreateAction);

        return builder.build(machineId);
    }
}