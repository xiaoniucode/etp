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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentStateMachineConfig {
    @Autowired
    private AuthAction authAction;
    @Autowired
    private ProxyCreateAction proxyCreateAction;
    @Autowired
    private ProxyInitAction proxyInitAction;

    @Bean("agentStateMachine")
    public StateMachine<AgentState, AgentEvent, AgentContext> createStateMachine() {
        StateMachineBuilder<AgentState, AgentEvent, AgentContext> builder = StateMachineBuilderFactory.create();
        builder.externalTransition()
                .from(AgentState.IDLE)
                .to(AgentState.AUTHENTICATING)
                .on(AgentEvent.AUTH_START)
                .when(ctx -> true)
                .perform(authAction);

        builder.externalTransition()
                .from(AgentState.AUTHENTICATING)
                .to(AgentState.AUTHENTICATED)
                .on(AgentEvent.AUTH_SUCCESS)
                .when(ctx -> true).perform(proxyInitAction);

        builder.externalTransition()
                .from(AgentState.AUTHENTICATED)
                .to(AgentState.AUTHENTICATED)
                .on(AgentEvent.PROXY_CREATE_REQUEST)
                .when(ctx -> true)
                .perform(proxyCreateAction);

        return builder.build("agent-state-machine");
    }
}