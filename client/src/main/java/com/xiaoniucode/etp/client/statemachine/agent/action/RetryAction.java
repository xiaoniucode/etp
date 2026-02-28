package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;

public class RetryAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RetryAction.class);


    @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext ctx) {
//        try {
            logger.debug("执行重试");

//            RetryConfig retryConfig = ctx.getConfig().getAuthConfig().getRetry();
//            Thread.sleep(retryConfig.getMaxDelay() * 1000);
//            // 触发重新连接
//            ctx.getStateMachine().fireEvent(ClientState.FAILED, ClientEvent.CONNECT_ATTEMPT, ctx);
//        } catch (InterruptedException e) {
//            logger.error("重试被中断", e);
//            Thread.currentThread().interrupt();
//        }
    }
}
