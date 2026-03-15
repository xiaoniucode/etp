package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.common.utils.AppVersionUtil;
import com.xiaoniucode.etp.client.common.utils.OSUtils;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public class AuthAction extends AgentBaseAction {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext ctx) {
        logger.debug("连接成功");
        ctx.setRetryCount(0);
        try {
            logger.debug("开始认证");
            AppConfig config = ctx.getConfig();
            Channel control = ctx.getControl();
            // 构建认证消息
            Message.AuthInfo authInfo = Message.AuthInfo.newBuilder()
                    .setToken(config.getAuthConfig().getToken())
                    .setClientId("1001001")//todo test clientId
                    .setVersion(AppVersionUtil.getVersion())
                    .setClientType(Message.ClientType.BINARY_DEVICE)
                    .setOs(OSUtils.getOS())
                    .setName(OSUtils.getHostName())
                    .build();
            // 发送认证消息
            ByteBuf buf = ProtobufUtil.toByteBuf(authInfo, control.alloc());
            TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_AUTH, buf);
            control.writeAndFlush(frame).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    logger.debug("认证消息发送成功");
                } else {
                    logger.error("认证消息发送失败", f.cause());
                    ctx.getStateMachine().fireEvent(AgentState.AUTHENTICATING, AgentEvent.AUTH_FAILURE, ctx);
                }
            });
        } catch (Exception e) {
            logger.error("认证失败", e);
            ctx.getStateMachine().fireEvent(AgentState.AUTHENTICATING, AgentEvent.AUTH_FAILURE, ctx);
        }
    }
}
