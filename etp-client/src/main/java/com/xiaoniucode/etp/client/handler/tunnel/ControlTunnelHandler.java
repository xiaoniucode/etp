package com.xiaoniucode.etp.client.handler.tunnel;

import com.xiaoniucode.etp.client.handler.factory.MessageHandlerFactory;
import com.xiaoniucode.etp.client.manager.domain.AgentSession;
import com.xiaoniucode.etp.client.manager.AgentSessionManager;
import com.xiaoniucode.etp.client.manager.ServerSessionManager;
import com.xiaoniucode.etp.core.handler.MessageHandler;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import com.xiaoniucode.etp.core.message.Message.*;

/**
 *
 * @author liuxin
 */
@ChannelHandler.Sharable
public class ControlTunnelHandler extends SimpleChannelInboundHandler<ControlMessage> {
    private final Logger logger = LoggerFactory.getLogger(ControlTunnelHandler.class);
    private final Consumer<ChannelHandlerContext> channelStatusCallback;

    public ControlTunnelHandler(Consumer<ChannelHandlerContext> channelStatusCallback) {
        this.channelStatusCallback = channelStatusCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ControlMessage msg) throws Exception {
        MessageType type = msg.getHeader().getType();
        MessageHandler handler = MessageHandlerFactory.getHandler(type);
        if (handler != null) {
            handler.handle(ctx, msg);
        }
    }

    /**
     * 代理客户端断开
     * 1. 清理所有隧道连接资源
     * 2. 清理代理客户端资源
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //清理和关闭隧道会话和连接
        ServerSessionManager.removeAllServerSession();
        //清理代理客户端资源
        AgentSessionManager.removeAgentSession(new Consumer<AgentSession>() {
            @Override
            public void accept(AgentSession agent) {
                if (agent == null) {
                    return;
                }
                logger.debug("代理客户端断开连接 - [客户端标识={}，会话标识={}]", agent.getClientId(), agent.getSessionId());
            }
        });
        channelStatusCallback.accept(ctx);
        super.channelInactive(ctx);
    }
}
