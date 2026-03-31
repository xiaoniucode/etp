package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.config.domain.TokenInfo;
import com.xiaoniucode.etp.server.config.domain.AgentInfo;
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.security.TokenManager;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.store.AgentStore;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AuthAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(AuthAction.class);
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private UUIDGenerator uuidGenerator;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private AgentStore agentStore;

    /**
     * 检查 Token 是否存在
     * 检查Token 并发限制
     * todo 检查AgentId是否已经认证在线，避免重复登录 检查是否是断线重连
     * 检查agentId是否存在，不存在则创建 --> 检查该Token下已注册Agent数是否超过限制
     *
     */
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Channel control = context.getControl();
        Message.AuthInfo authInfo = context.getAndRemoveAs(AgentConstants.AGENT_AUTH_INFO, Message.AuthInfo.class);
        String token = authInfo.getToken();
        if (!tokenManager.checkToken(token)) {
            logger.error("客户端认证失败，无效令牌：{}", token);
            Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                    .setCode(1)
                    .setMessage("认证失败，无效令牌:" + token).build();
            sendAuthError(control, authResponse);
            context.fireEvent(AgentEvent.AUTH_FAILURE);
            return;
        }
        TokenInfo tokenInfo = tokenManager.getAccessToken(token);
        if (!tokenManager.checkConnectionsLimit(token)) {
            logger.warn("访问令牌 {} 连接数已达上限 {}", token, tokenInfo.getMaxConnections());
            Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                    .setCode(1)
                    .setMessage("访问令牌 " + token + " 连接数已达上限:" + tokenInfo.getMaxConnections()).build();
            sendAuthError(control, authResponse);
            context.fireEvent(AgentEvent.AUTH_FAILURE);
            return;
        }

        //如果没有 agentId 则生成
        String agentId = authInfo.getAgentId();
        AgentInfo oldAgentInfo = null;
        if (!StringUtils.hasText(agentId)) {
            if (!tokenManager.checkAgentLimit(token)) {
                logger.warn("访问令牌 {} 客户端注册数已达上限 {}", token, tokenInfo.getMaxClients());
                Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                        .setCode(1)
                        .setMessage("访问令牌 " + token + " 客户端注册数已达上限:" + tokenInfo.getMaxClients()).build();
                sendAuthError(control, authResponse);
                context.fireEvent(AgentEvent.AUTH_FAILURE);
                return;
            } else {
                agentId = uuidGenerator.uuid16(true);
            }
        } else {
            //如果携带了 agentId 需要检查是否合法
            Optional<AgentInfo> agentInfoOpt = agentStore.findById(agentId);
            if (agentInfoOpt.isEmpty()) {
                logger.warn("设备ID {} 不存在", agentId);
                Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                        .setCode(1)
                        .setMessage("AgentId " + agentId + " 不存在，二进制设备请删除 agent.id 文件").build();
                sendAuthError(control, authResponse);
                context.fireEvent(AgentEvent.AUTH_FAILURE);
                return;
            } else {
                oldAgentInfo = agentInfoOpt.get();
            }

        }
        AgentInfo agentInfo = createOrUpdateAgentInfo(agentId, oldAgentInfo, authInfo);
        context.setControl(control);
        context.setAgentInfo(agentInfo);

        //保存设备信息
        agentStore.save(agentInfo);
        tokenManager.incrementConnection(token);
        agentManager.addClientContextIndex(agentId, context);
        Message.AuthResponse authResponse = Message.AuthResponse.newBuilder().setCode(0)
                .setConnectionId(context.getConnectionId())
                .setAgentId(agentId)
                .setMessage("认证成功")
                .build();

        TMSPFrame authFrame = new TMSPFrame(0, TMSP.MSG_AUTH_RESP);
        ByteBuf payload = ProtobufUtil.toByteBuf(authResponse, control.alloc());
        authFrame.setPayload(payload);

        context.fireEvent(AgentEvent.AUTH_SUCCESS);
        control.writeAndFlush(authFrame).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                logger.error("发送认证成功消息失败", future.cause());
            }
        });
        logger.debug("设备认证成功：[设备ID={}，设备类型={}，版本号={}]", agentId, agentInfo.getAgentType(), agentInfo.getVersion());
    }

    private AgentInfo createOrUpdateAgentInfo(String agentId, AgentInfo oldAgentInfo, Message.AuthInfo authInfo) {
        AgentInfo agentInfo = oldAgentInfo == null ? new AgentInfo() : oldAgentInfo;
        agentInfo.setToken(authInfo.getToken());
        agentInfo.setAgentId(agentId);
        agentInfo.setAgentType(getAgentType(authInfo));
        agentInfo.setVersion(authInfo.getVersion());
        agentInfo.setOs(authInfo.getOs());
        agentInfo.setArch(authInfo.getArch());

        if (oldAgentInfo == null) {
            agentInfo.setCreatedAt(LocalDateTime.now());
        }
        agentInfo.setLastActiveTime(LocalDateTime.now());
        agentInfo.setOnline(false);

        return agentInfo;
    }

    private AgentType getAgentType(Message.AuthInfo authInfo) {
        switch (authInfo.getAgentType()) {
            case BINARY -> {
                return AgentType.BINARY;
            }
            case SESSION -> {
                return AgentType.SESSION;
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
