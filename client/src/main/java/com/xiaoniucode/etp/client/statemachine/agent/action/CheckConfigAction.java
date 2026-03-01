package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.common.utils.StringUtils;

public class CheckConfigAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CheckConfigAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext ctx) {
        logger.debug("检查客户端配置");
        AppConfig config = ctx.getConfig();
        AuthConfig auth = config.getAuthConfig();
        if (!StringUtils.hasText(auth.getToken())) {
            ctx.setConfigValid(false);
            ctx.getStateMachine().fireEvent(ctx.getState(), AgentEvent.STOP, ctx);
            logger.error("请配置登陆密钥");
        } else {
            ctx.setConfigValid(true);
            ctx.getStateMachine().fireEvent(ctx.getState(), AgentEvent.CONFIG_CHECKED, ctx);
        }
    }
}
