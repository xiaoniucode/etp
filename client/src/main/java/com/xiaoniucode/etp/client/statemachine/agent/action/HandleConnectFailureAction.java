package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;

public class HandleConnectFailureAction extends AgentBaseAction{
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HandleConnectFailureAction.class);
      @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext context) {

    }
}
