package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.common.utils.AppVersionUtil;
import com.xiaoniucode.etp.client.common.utils.OSUtils;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;

public class AuthAction extends AgentBaseAction {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AuthAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext ctx) {
        try {
            logger.debug("开始认证");
            AppConfig config = ctx.getConfig();
            AuthConfig authConfig = config.getAuthConfig();
            Channel control = ctx.getControl();
            Message.AgentType agentType = toProto(config.getAgentType());

            Message.AuthInfo authInfo = Message.AuthInfo.newBuilder()
                    .setToken(authConfig.getToken())
                    .setVersion(AppVersionUtil.getVersion())
                    .setAgentType(agentType)
                    .setOs(OSUtils.getOS())
                    .setName(OSUtils.getHostName())
                    .build();

            ByteBuf buf = ProtobufUtil.toByteBuf(authInfo, control.alloc());
            TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_AUTH, buf);
            control.writeAndFlush(frame).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    logger.debug("认证消息发送成功");
                } else {
                    logger.error("认证失败", f.cause());
                    ctx.fireEvent(AgentEvent.STOP);
                }
            });
        } catch (Exception e) {
            logger.error("认证失败", e);
            ctx.fireEvent(AgentEvent.STOP);
        }
    }

    private Message.AgentType toProto(AgentType agentType) {
        if (agentType == null) {
            return Message.AgentType.UNRECOGNIZED;
        }
        switch (agentType) {
            case BINARY:
                return Message.AgentType.BINARY;
            case SESSION:
                return Message.AgentType.SESSION;
            default:
                return Message.AgentType.UNRECOGNIZED;
        }
    }

}
