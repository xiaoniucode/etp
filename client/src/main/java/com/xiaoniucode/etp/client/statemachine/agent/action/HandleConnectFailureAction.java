package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;

public class HandleConnectFailureAction extends AgentBaseAction{
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HandleConnectFailureAction.class);
      @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

    }
}
