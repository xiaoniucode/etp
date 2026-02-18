package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.handler.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
import com.xiaoniucode.etp.server.event.AgentLoginEvent;
import com.xiaoniucode.etp.server.handler.utils.MessageUtils;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
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
    @Autowired
    private AccessTokenManager accessTokenManager;
    @Autowired
    private EventBus eventBus;

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage msg) {
        Channel control = ctx.channel();
        Message.Login login = msg.getLogin();
        String clientId = login.getClientId();
        String token = login.getToken();
        //todo 检查是否已经登陆了，避免重复登陆
        boolean hasToken = accessTokenManager.hasToken(token);
        if (!hasToken) {
            logger.error("客户端 - {} 认证失败，无效令牌：{}", clientId, token);
            ControlMessage message = MessageUtils.buildErrorMessage(401, "认证失败，无效令牌: " + token);
            control.writeAndFlush(message).addListener(future -> {
                if (future.isSuccess()) {
                    control.close();
                }
            });
            return;
        }
        // 已经连接的代理客户端数量
        Integer agents = agentSessionManager.getOnlineAgents(token);
        AccessTokenInfo accessToken = accessTokenManager.getAccessToken(token);
        Integer maxClient = accessToken.getMaxClients();
        //macClient=-1表示不限制Token连接数
        if (maxClient != -1 && agents >= maxClient) {
            logger.warn("访问令牌连接数已达上限: {}", maxClient);
            ControlMessage message = MessageUtils.buildErrorMessage(401, "访问令牌连接数已达上限: " + maxClient);
            control.writeAndFlush(message).addListener(future -> {
                if (future.isSuccess()) {
                    control.close();
                }
            });
            return;
        }
        ClientType type = null;
        Message.ClientType clientType = login.getClientType();
        switch (clientType) {
            case WEB_SESSION -> type = ClientType.WEB_SESSION;
            case BINARY_DEVICE -> type = ClientType.BINARY_DEVICE;
        }
        //创建代理客户端会话上下文
        AgentSession.AgentSessionBuilder builder = AgentSession.builder()
                .clientId(clientId)
                .name(login.getName())
                .clientType(type)
                .token(token)
                .control(control)
                .arch(login.getArch())
                .os(login.getOs())
                .version(login.getVersion());

        agentSessionManager.createAgentSession(builder)
                .ifPresent(session -> {
                    eventBus.publishAsync(new AgentLoginEvent(session.isNew(), session));
                    //返回登陆成功消息
                    ControlMessage message = MessageUtils.buildLoginResp(session.getSessionId());
                    control.writeAndFlush(message).addListener(future -> {
                        if (!future.isSuccess()) {
                            logger.warn("登陆成功返回结果消息发送失败");
                        }
                    });
                    logger.debug("客户端登陆成功：[客户端标识={}，令牌={}，版本号={}]", clientId, token, session.getVersion());
                });
    }
}
