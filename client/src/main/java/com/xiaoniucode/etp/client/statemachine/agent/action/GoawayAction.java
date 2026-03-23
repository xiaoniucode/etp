package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoawayAction extends AgentBaseAction{
    private final Logger logger= LoggerFactory.getLogger(GoawayAction.class);
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

    }
}
