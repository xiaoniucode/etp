package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyCreateResponseAction extends AgentBaseAction {
    private final Logger logger= LoggerFactory.getLogger(ProxyCreateResponseAction.class);
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Message.NewProxyResp newProxyResp = context.getVariableAs("NEW_PROXY_RESP", Message.NewProxyResp.class);
        logger.info("远程访问地址：{}",newProxyResp.getRemoteAddr());
    }
}
