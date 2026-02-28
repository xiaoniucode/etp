package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;

public class StopAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StopAction.class);



    @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext ctx) {
        logger.debug("停止客户端");
        ctx.setStopped(true);
        // 清理资源
        if (ctx.getControlWorkerGroup() != null) {
            ctx.getControlWorkerGroup().shutdownGracefully();
        }
    }
}