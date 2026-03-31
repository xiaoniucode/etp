package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.transport.AbstractAgentContext;
import com.xiaoniucode.etp.server.config.domain.AgentInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AgentContext extends AbstractAgentContext {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(AgentContext.class);
    private AgentState state = AgentState.NEW;
    private AgentInfo agentInfo;
    private LocalDateTime lastActiveTime;
    private StateMachine<AgentState, AgentEvent, AgentContext> stateMachine;

    public AgentContext(StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine) {
        stateMachine = agentStateMachine;
        this.lastActiveTime = LocalDateTime.now();
    }

    public void fireEvent(AgentEvent event) {
        stateMachine.fireEvent(state, event, this);
    }
    public void updateActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
        if (agentInfo != null) {
            agentInfo.setLastActiveTime(this.lastActiveTime);
        }
    }
}
