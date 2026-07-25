package io.github.lxien.orbien.client.statemachine.agent.action;

import io.github.lxien.orbien.client.statemachine.ContextConstants;
import io.github.lxien.orbien.client.statemachine.agent.AgentContext;
import io.github.lxien.orbien.client.statemachine.agent.AgentEvent;
import io.github.lxien.orbien.client.statemachine.agent.AgentState;
import io.github.lxien.orbien.core.enums.AgentType;
import io.github.lxien.orbien.core.enums.AuthStatusCode;
import io.github.lxien.orbien.core.message.Message;
import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class AuthRespAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(AuthRespAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {

        Message.AuthResponse authResponse = context.getAndRemoveAs(ContextConstants.AUTH_RESP,
                Message.AuthResponse.class);
        Message.Status status = authResponse.getStatus();
        AuthStatusCode statusCode = AuthStatusCode.fromCode(status.getCode());

        if (statusCode != null && statusCode.isSuccess()) {
            logger.info("认证成功");
            String agentId = authResponse.getAgentId();
            context.setConnectionId(authResponse.getConnectionId());
            context.setAuthenticated(true);
            AgentType agentType = context.getAgentType();
            context.getAgentIdentity().updateIdentity(agentId, agentType.isStandalone());
            context.fireEvent(AgentEvent.AUTH_SUCCESS);
            return;
        }

        if (statusCode != null && statusCode.isRecoverable() && context.getAgentType().isStandalone()) {
            logger.warn("本地设备ID在服务端不存在，已清除本地身份并重新认证: {}", status.getMessage());
            context.getAgentIdentity().clearIdentity();
            context.getRetryCount().set(0);
            Channel control = context.getControl();
            if (control != null && control.isActive()) {
                ChannelUtils.closeOnFlush(control);
            } else {
                context.fireEvent(AgentEvent.DISCONNECT);
            }
            return;
        }

        logger.error("认证失败: {}", status.getMessage());
        context.fireEvent(AgentEvent.LOCAL_GOAWAY);
    }
}
