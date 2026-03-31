package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.message.Message;
import org.slf4j.Logger;

public class AuthResponseAction extends AgentBaseAction {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AuthResponseAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

        Message.AuthResponse authResponse = context.getAndRemoveAs("authResponse", Message.AuthResponse.class);
        int code = authResponse.getCode();
        if (code == 0) {
            logger.info("认证成功，客户端类型：[{}] 唯一标识：{}", context.getAgentType().name(), authResponse.getAgentId());
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
