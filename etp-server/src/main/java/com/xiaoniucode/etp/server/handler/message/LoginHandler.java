package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.manager.AccessTokenManager;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.ControlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理客户端连接认证
 *
 * @author liuxin
 */
@Component
public class LoginHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    @Autowired
    private AgentSessionManager agentSessionManager;

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage msg) {
        Channel control = ctx.channel();
        Message.Login login = msg.getLogin();
        String token = login.getToken();
        boolean hasToken = AccessTokenManager.hasToken(token);
//        if (!hasToken) {
//            logger.error("无效Token：{}", token);
//            ControlMessage message = ControlMessageWrapper.buildErrorMessage(401, "无效 Token");
//            control.writeAndFlush(message).addListener(future -> {
//                if (future.isSuccess()) {
//                    control.close();
//                }
//            });
//            return;
//        }
        //已经连接的代理客户端数量
//        Integer agents = agentSessionManager.getAgents(token);
//        AccessTokenInfo accessToken = AccessTokenManager.getAccessToken(token);
//        Integer maxClient = accessToken.getMaxClient();
//        //macClient=-1表示不限制Token连接数
//        if (maxClient != -1 && agents > maxClient) {
//            ControlMessage message = ControlMessageWrapper.buildErrorMessage(401, "Token 连接数达到限制");
//            control.writeAndFlush(message).addListener(future -> {
//                if (future.isSuccess()) {
//                    control.close();
//                }
//            });
//        }
        //认证通过，注册Agent连接
        AgentSession agent = new AgentSession(control, token);
        agent.setArch(login.getArch());
        agent.setOs(login.getOs());
        agentSessionManager.registerAgent(agent);
        logger.debug("Agent: {} login success", token);
        //可返回登陆成功消息给客户端 Agent
    }
}
