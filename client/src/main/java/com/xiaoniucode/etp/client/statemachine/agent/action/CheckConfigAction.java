package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;
import com.xiaoniucode.etp.common.utils.StringUtils;

public class CheckConfigAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CheckConfigAction.class);

    @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext ctx) {
        logger.debug("检查客户端配置");
        AppConfig config = ctx.getConfig();
        AuthConfig auth = config.getAuthConfig();
        if (!StringUtils.hasText(auth.getToken())) {
            ctx.setConfigValid(false);
            ctx.getStateMachine().fireEvent(ctx.getState(), ClientEvent.STOP, ctx);
            logger.error("请配置登陆密钥");
        } else {
            ctx.setConfigValid(true);
            ctx.getStateMachine().fireEvent(ctx.getState(), ClientEvent.CONFIG_CHECKED, ctx);
        }
    }
}
