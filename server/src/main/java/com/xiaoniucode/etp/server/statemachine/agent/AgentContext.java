package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import com.xiaoniucode.etp.core.transport.AbstractAgentContext;
import io.netty.channel.Channel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class AgentContext extends AbstractAgentContext {
    private final Logger logger = LoggerFactory.getLogger(AgentContext.class);
    private AgentState state = AgentState.NEW;
    private ClientType clientType;
    private String clientId;
    private String token;
    private String version;
    private String os;
    private String arch;
    private String name;
    private StateMachine<AgentState, AgentEvent, AgentContext> stateMachine;

    public AgentContext(StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine) {
        stateMachine = agentStateMachine;
    }

    public void fireEvent(AgentEvent event) {
        stateMachine.fireEvent(state, event, this);
    }
}
