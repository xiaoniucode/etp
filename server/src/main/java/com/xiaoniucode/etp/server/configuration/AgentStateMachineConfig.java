package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.action.*;
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
    @Autowired
    private GoawayAction disconnectAction;
    @Autowired
    private CreateTunnelAction createTunnelAction;

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
                .to(AgentState.ESTABLISHED)
                .on(AgentEvent.AUTH_SUCCESS)
                .when(ctx -> true).perform(proxyInitAction);

        builder.externalTransition()
                .from(AgentState.ESTABLISHED)
                .to(AgentState.ESTABLISHED)
                .on(AgentEvent.PROXY_CREATE_REQUEST)
                .when(ctx -> true)
                .perform(proxyCreateAction);
        builder.externalTransition()
                .from(AgentState.ESTABLISHED)
                .to(AgentState.ESTABLISHED)
                .on(AgentEvent.CREATE_TUNNEL)
                .when(ctx -> true)
                .perform(createTunnelAction);
        builder.externalTransitions()
                .fromAmong(AgentState.ESTABLISHED, AgentState.FAILED, AgentState.AUTHENTICATING)
                .to(AgentState.DISCONNECTED)
                .on(AgentEvent.DISCONNECT)
                .when(ctx -> true)
                .perform(disconnectAction);
        return builder.build("agent-state-machine");
    }
}