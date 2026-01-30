package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.manager.AgentSession;
import com.xiaoniucode.etp.client.manager.AgentSessionManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.Message;
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
        String clientId = control.attr(EtpConstants.CLIENT_ID).get();
        AgentSession agentSession = new AgentSession(clientId, sessionId, control);
        AgentSessionManager.setAgentSession(agentSession);
        logger.info("登陆成功 - 客户端ID={}，会话标识={}", clientId, sessionId);
    }
}
