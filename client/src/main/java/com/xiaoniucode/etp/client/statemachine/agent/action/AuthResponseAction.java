package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.message.Message;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class AuthResponseAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(AuthResponseAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

        Message.AuthResponse authResponse = context.getAndRemoveAs("authResponse", Message.AuthResponse.class);
        int code = authResponse.getCode();
        if (code == 0) {
            logger.info("连接成功");
            String agentId = authResponse.getAgentId();
            context.setConnectionId(authResponse.getConnectionId());
            context.setAuthenticated(true);
            AgentType agentType = context.getAgentType();
            context.getAgentIdentity().updateIdentity(agentId, agentType.isBinary());
            context.fireEvent(AgentEvent.AUTH_SUCCESS);
        } else {
            logger.error("{}", authResponse.getMessage());
            context.fireEvent(AgentEvent.STOP);
        }
    }
}
