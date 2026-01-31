package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.handler.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.manager.AccessTokenManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.message.Message.ControlMessage;
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
        String clientId = login.getClientId();
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
        agentSessionManager.createAgentSession(clientId, token, control, login.getArch(), login.getOs(),login.getVersion()).ifPresent(agentSession -> {
            //返回登陆成功消息
            Message.MessageHeader heder = Message.MessageHeader.newBuilder().setType(Message.MessageType.LOGIN_RESP).build();
            Message.LoginResp loginResp = Message.LoginResp.newBuilder().setSessionId(agentSession.getSessionId()).build();

            ControlMessage message = ControlMessage.newBuilder().setHeader(heder).setLoginResp(loginResp).build();
            control.writeAndFlush(message).addListener(future -> {
                if (!future.isSuccess()) {
                    logger.warn("登陆成功返回结果消息发送失败");
                }
            });
            logger.debug("客户端登陆成功：[客户端ID={}，令牌={}，版本号={}]", clientId, token, agentSession.getVersion());
        });
    }
}
