package io.github.lxien.orbien.server.statemachine.agent.action;

import io.github.lxien.orbien.server.statemachine.agent.*;
import io.github.lxien.orbien.server.uid.UidGenerator;
import io.github.lxien.orbien.core.enums.AgentType;
import io.github.lxien.orbien.core.enums.AuthStatusCode;
import io.github.lxien.orbien.core.message.Message;
import io.github.lxien.orbien.core.message.TMSP;
import io.github.lxien.orbien.core.message.TMSPFrame;
import io.github.lxien.orbien.server.notify.EventBus;
import io.github.lxien.orbien.core.utils.ProtobufUtil;
import io.github.lxien.orbien.server.event.AgentAuthEvent;
import io.github.lxien.orbien.server.service.AgentConfigService;
import io.github.lxien.orbien.server.service.TokenConfigService;
import io.github.lxien.orbien.server.utils.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Component
public class AuthAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(AuthAction.class);
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private TokenConfigService tokenConfigService;
    @Autowired
    private UidGenerator uuidGenerator;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private AgentConfigService agentConfigService;

    /**
     * 检查 Token 是否存在
     * 检查Token 并发限制
     * 检查AgentId是否已经认证在线，避免重复登录 检查是否是断线重连
     * 检查agentId是否存在，不存在则创建 --> 检查该Token下已注册Agent数是否超过限制
     *
     */
    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Channel control = context.getControl();
        Message.AuthInfo authInfo = context.getAndRemoveAs(AgentConstants.AGENT_AUTH_INFO, Message.AuthInfo.class);
        //是否是重连
        boolean isReconnect = (event == AgentEvent.RETRY_CONNECT);

        if (isReconnect) {
            AgentInfo existAgentInfo = context.getAgentInfo();
            String token = authInfo.getToken();
            if (!Objects.equals(token, existAgentInfo.getToken())) {
                logger.warn("断线重连认证失败，令牌不匹配，当前令牌：{}，历史令牌：{}", token, context.getAgentInfo().getToken());
                rejectAuth(control, context, AuthStatusCode.FAILURE, "重连认证失败，令牌不匹配");
                return;
            }
            String agentId = authInfo.getAgentId();
            if (!StringUtils.hasText(agentId) || !Objects.equals(agentId, existAgentInfo.getAgentId())) {
                logger.warn("断线重连认证失败，设备ID不匹配，当前设备ID：{}，历史设备ID：{}", agentId, context.getAgentInfo().getAgentId());
                rejectAuth(control, context, AuthStatusCode.FAILURE, "重连认证失败，设备ID不匹配");
                return;
            }
        }
        String token = authInfo.getToken();
        if (!tokenConfigService.existsByToken(token) && !isReconnect) {
            logger.error("客户端认证失败，无效令牌：{}", token);
            rejectAuth(control, context, AuthStatusCode.INVALID_TOKEN, "认证失败，无效令牌:" + token);
            return;
        }

        String agentId = authInfo.getAgentId();
        AgentInfo oldAgentInfo = null;
        AgentType clientAgentType = getAgentType(authInfo);
        boolean sessionClient = clientAgentType != null && clientAgentType.isSession();

        if (!StringUtils.hasText(agentId)) {
            agentId = uuidGenerator.getUIDAsString();
        } else {
            Optional<AgentInfo> agentInfoOpt = agentConfigService.findById(agentId);
            if (agentInfoOpt.isEmpty()) {
                if (sessionClient) {
                    agentId = uuidGenerator.getUIDAsString();
                    logger.debug("会话型客户端分配新 agentId: {}", agentId);
                } else {
                    logger.warn("设备ID {} 不存在", agentId);
                    rejectAuth(control, context, AuthStatusCode.AGENT_NOT_FOUND,
                            "AgentId " + agentId + " 不存在");
                    return;
                }
            } else {
                oldAgentInfo = agentInfoOpt.get();
            }
        }
        AgentInfo agentInfo = createOrUpdateAgentInfo(agentId, oldAgentInfo, authInfo);
        agentInfo.setSourceIp(NetUtils.getIp(control));
        context.setControl(control);
        context.setAgentInfo(agentInfo);

        agentManager.addAgentContextIndex(agentId, context);


        Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                .setStatus(status(AuthStatusCode.SUCCESS, AuthStatusCode.SUCCESS.getDescription()))
                .setConnectionId(context.getConnectionId())
                .setAgentId(agentId)
                .build();
        TMSPFrame authFrame = new TMSPFrame(0, TMSP.MSG_AUTH_RESP);
        ByteBuf payload = ProtobufUtil.toByteBuf(authResponse, control.alloc());
        authFrame.setPayload(payload);

        control.writeAndFlush(authFrame).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                logger.error("发送认证成功消息失败", future.cause());
            }
        });
        eventBus.publishAsync(new AgentAuthEvent(agentInfo, isReconnect));
        context.fireEvent(AgentEvent.AUTH_SUCCESS);
        logger.debug("设备认证成功：[设备ID={}，设备类型={}，版本号={}]", agentId, agentInfo.getAgentType(), agentInfo.getVersion());
    }

    private void rejectAuth(Channel control, AgentContext context, AuthStatusCode statusCode, String message) {
        Message.AuthResponse authResponse = Message.AuthResponse.newBuilder()
                .setStatus(status(statusCode, message))
                .build();
        ByteBuf payload = ProtobufUtil.toByteBuf(authResponse, control.alloc());
        TMSPFrame authFrame = new TMSPFrame(0, TMSP.MSG_AUTH_RESP);
        authFrame.setPayload(payload);
        control.writeAndFlush(authFrame).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                logger.warn("发送认证失败响应失败", future.cause());
            }
            context.fireEvent(AgentEvent.AUTH_FAILURE);
        });
    }

    private Message.Status status(AuthStatusCode statusCode, String message) {
        return Message.Status.newBuilder()
                .setCode(statusCode.getCode())
                .setMessage(message)
                .build();
    }

    private AgentInfo createOrUpdateAgentInfo(String agentId, AgentInfo oldAgentInfo, Message.AuthInfo authInfo) {
        AgentInfo agentInfo = oldAgentInfo == null ? new AgentInfo() : oldAgentInfo;
        agentInfo.setName(authInfo.getName());
        agentInfo.setToken(authInfo.getToken());
        agentInfo.setAgentId(agentId);
        agentInfo.setAgentType(getAgentType(authInfo));
        agentInfo.setVersion(authInfo.getVersion());
        agentInfo.setOs(authInfo.getOs());
        agentInfo.setArch(authInfo.getArch());
        agentInfo.setToken(authInfo.getToken());

        if (oldAgentInfo == null) {
            agentInfo.setCreatedAt(LocalDateTime.now());
        }
        agentInfo.setLastActiveTime(LocalDateTime.now());
        return agentInfo;
    }

    private AgentType getAgentType(Message.AuthInfo authInfo) {
        switch (authInfo.getAgentType()) {
            case BINARY -> {
                return AgentType.STANDALONE;
            }
            case SESSION -> {
                return AgentType.SESSION;
            }
        }
        return null;
    }
}
