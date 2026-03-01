package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;

public class StopAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StopAction.class);



    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext ctx) {
        logger.debug("停止客户端");
        ctx.setStopped(true);
        // 清理资源
        if (ctx.getControlWorkerGroup() != null) {
            ctx.getControlWorkerGroup().shutdownGracefully();
        }
    }
}