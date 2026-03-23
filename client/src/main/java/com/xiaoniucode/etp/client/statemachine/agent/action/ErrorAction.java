package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorAction extends AgentBaseAction{
    private final Logger logger= LoggerFactory.getLogger(ErrorAction.class);
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Message.Error error = context.getAndRemoveAs("ERROR", Message.Error.class);
        logger.error(error.getMessage());
    }
}
