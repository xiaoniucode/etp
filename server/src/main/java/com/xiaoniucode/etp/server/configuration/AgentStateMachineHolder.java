package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgentStateMachineHolder {
    private static AgentStateMachineConfig config;

    @Autowired
    public void setConfig(AgentStateMachineConfig config) {
        AgentStateMachineHolder.config = config;
    }

    public static StateMachine<AgentState, AgentEvent, AgentContext> get(int connectionId) {
        return config.createStateMachine("agent:" + connectionId);
    }
}
