package com.xiaoniucode.etp.server.statemachine.agent;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class AgentContext extends ProcessContextImpl {
    private final Logger logger = LoggerFactory.getLogger(AgentContext.class);
    private AgentState state = AgentState.IDLE;
    private Integer connectionId;
    private Channel control;
    private String clientId;
    private String token;
    private String version;
    private ClientType clientType;
    private String os;
    private String arch;
    private String name;
    private StateMachine<AgentState, AgentEvent, AgentContext> stateMachine;
    private AgentManager agentManager;

    public AgentContext(AgentManager agentManager, StateMachine<AgentState, AgentEvent, AgentContext> agentStateMachine) {
        this.agentManager = agentManager;
        stateMachine = agentStateMachine;
    }

    public void fireEvent(AgentEvent event) {
        stateMachine.fireEvent(getState(), event, this);
    }
}
