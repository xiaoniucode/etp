package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.domain.RetryConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;

public class HandleAuthFailureAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HandleAuthFailureAction.class);


    @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext ctx) {
        logger.debug("连接失败");
        int retryCount = ctx.getRetryCount() + 1;
        ctx.setRetryCount(retryCount);

        RetryConfig retryConfig = ctx.getConfig().getAuthConfig().getRetry();
        if (retryCount <= retryConfig.getMaxRetries()) {
            logger.debug("准备重试连接，第 {} 次", retryCount);
            // 触发重试事件
            ctx.getStateMachine().fireEvent(ClientState.FAILED, ClientEvent.RETRY, ctx);
        } else {
            logger.error("达到最大重试次数，停止尝试");
            ctx.getStateMachine().fireEvent(ClientState.FAILED, ClientEvent.STOP, ctx);
        }
    }
}