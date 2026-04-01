package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.transport.AbstractAgentContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AgentContext extends AbstractAgentContext {
    private AgentState state = AgentState.NEW;
    private AgentInfo agentInfo;
    private StateMachine<AgentState, AgentEvent, AgentContext> stateMachine;

    public AgentContext(StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine) {
        stateMachine = agentStateMachine;
        updateActiveTime();
    }

    public void fireEvent(AgentEvent event) {
        stateMachine.fireEvent(state, event, this);
    }

    @Override
    public void updateActiveTime() {
        super.updateActiveTime();
        if (agentInfo != null) {
            agentInfo.setLastActiveTime(this.getLastActiveTime());
        }
    }
}
