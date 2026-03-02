package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.manager.AccessTokenManager;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuthAction extends AgentBaseAction {
    private final Logger logger = LoggerFactory.getLogger(AuthAction.class);
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private AccessTokenManager accessTokenManager;
    @Autowired
    private UUIDGenerator uuidGenerator;
    @Autowired
    private EventBus eventBus;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Channel control = context.getControl();
        Message.AuthInfo authInfo = context.getVariableAs(AgentConstants.AGENT_AUTH_INFO, Message.AuthInfo.class);
        String token = authInfo.getToken();
        boolean hasToken = accessTokenManager.hasToken(token);
        if (!hasToken) {
            context.fireEvent(AgentEvent.AUTH_FAILURE);
            logger.error("客户端认证失败，无效令牌：{}", token);
            Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                    .setCode(1)
                    .setMessage("认证失败，无效令牌:" + token).build();
            sendAuthError(control, authResponse);
            return;
        }
        int count = 1;//todo
        AccessTokenInfo accessToken = accessTokenManager.getAccessToken(token);
        Integer maxClient = accessToken.getMaxClients();

        //macClient=-1表示不限制Token连接数
        if (maxClient != -1 && count >= maxClient) {
            // context.fireEvent(AgentEvent.AUTH_FAILURE);
            logger.warn("访问令牌连接数已达上限: {}", maxClient);
            Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                    .setCode(1)
                    .setMessage("访问令牌连接数已达上限:" + token).build();
            sendAuthError(control, authResponse);
            return;
        }

        String clientId = authInfo.getClientId();

        if (!StringUtils.hasText(clientId)) {
            clientId = uuidGenerator.uuid32();
        }

        context.setControl(control);
        context.setToken(token);
        context.setClientId(clientId);
        context.setClientType(getClientType(authInfo));
        context.setVersion(authInfo.getVersion());
        context.setOs(authInfo.getOs());
        context.setArch(authInfo.getArch());

        Message.AuthResponse authResponse = Message.AuthResponse.newBuilder().setCode(0)
                .setConnectionId(context.getConnectionId())
                .setClientId(clientId)
                .setMessage("认证成功")
                .build();

        TMSPFrame authFrame = new TMSPFrame(0, TMSP.MSG_AUTH_RESP);
        authFrame.setPayload(ProtobufUtil.toByteBuf(authResponse, control.alloc()));

        //todo eventBus.publishAsync(new AgentAuthEvent(context));
        context.fireEvent(AgentEvent.AUTH_SUCCESS);

        control.writeAndFlush(authFrame);
        context.removeVariable(AgentConstants.AGENT_AUTH_INFO);
        logger.debug("客户端认证成功：[客户端ID={}，版本号={}]", context.getClientId(), context.getVersion());
    }


    private ClientType getClientType(Message.AuthInfo authInfo) {
        switch (authInfo.getClientType()) {
            case BINARY_DEVICE -> {
                return ClientType.BINARY_DEVICE;
            }
            case WEB_SESSION -> {
                return ClientType.WEB_SESSION;
            }
        }
        return null;
    }

    public void sendAuthError(Channel control, Message.AuthResponse authResponse) {
        ByteBuf payload = ProtobufUtil.toByteBuf(authResponse, control.alloc());
        TMSPFrame authFrame = new TMSPFrame(0, TMSP.MSG_AUTH_RESP);
        authFrame.setPayload(payload);
        control.writeAndFlush(authFrame);
    }
}
