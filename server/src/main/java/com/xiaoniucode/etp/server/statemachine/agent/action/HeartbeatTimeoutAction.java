package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import org.springframework.stereotype.Component;

@Component
public class HeartbeatTimeoutAction  extends AgentBaseAction{
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

    }
}
