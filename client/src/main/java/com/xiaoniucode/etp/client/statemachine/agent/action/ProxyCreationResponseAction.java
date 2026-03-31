package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.message.Message;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class ProxyCreationResponseAction extends AgentBaseAction {
    private final InternalLogger logger= InternalLoggerFactory.getInstance(ProxyCreationResponseAction.class);
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Message.NewProxyResp newProxyResp = context.getAndRemoveAs("NEW_PROXY_RESP", Message.NewProxyResp.class);
        logger.info("远程访问地址：{}",newProxyResp.getRemoteAddr());
    }
}
