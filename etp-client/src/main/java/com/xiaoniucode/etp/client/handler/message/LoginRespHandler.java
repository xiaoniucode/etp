package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.event.LoginSuccessEvent;
import com.xiaoniucode.etp.client.manager.AgentSessionManager;
import com.xiaoniucode.etp.client.manager.EventBusManager;
import com.xiaoniucode.etp.core.handler.MessageHandler;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.Message.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginRespHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(LoginRespHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, Message.ControlMessage msg) throws Exception {
        Message.LoginResp loginResp = msg.getLoginResp();
        String sessionId = loginResp.getSessionId();
        Channel control = ctx.channel();

        AgentSessionManager.createAgentSession(sessionId, control).ifPresent(agent -> {
            String clientId = agent.getClientId();
            logger.info("登陆成功 - [客户端标识={}]", clientId);
            EventBusManager.publishAsync(new LoginSuccessEvent(clientId));
        });
    }
}
