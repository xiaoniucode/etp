package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.handler.MessageHandler;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.handler.factory.TunnelMessageHandlerFactory;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.message.Message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 控制指令处理，负责消息分发
 *
 * @author liuxin
 */
@Component
@ChannelHandler.Sharable
public class ControlTunnelHandler extends SimpleChannelInboundHandler<ControlMessage> {
    private final Logger logger = LoggerFactory.getLogger(ControlTunnelHandler.class);
    @Autowired
    private TunnelMessageHandlerFactory factory;
    @Autowired
    private AgentSessionManager agentSessionManager;
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ControlMessage msg) throws Exception {
        try {
            MessageType messageType = msg.getHeader().getType();
            MessageHandler handler = factory.getHandler(messageType);
            if (handler != null) {
                handler.handle(ctx, msg);
            }
            ctx.fireChannelRead(msg);
        } catch (Exception e) {
            logger.error("消息处理器分发异常",e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("客户端连接断开，开始释放资源");
        Channel control = ctx.channel();
        agentSessionManager.disconnect(control);
        Set<Integer> remotePorts = agentSessionManager.getAgentRemotePorts(control);
        Set<String> domains = agentSessionManager.getAgentDomains(control);
        visitorSessionManager.disconnectAllSessionsForAgent(control,remotePorts,domains);
        ChannelUtils.closeOnFlush(control);
        logger.debug("客户端连接资源释放完成");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error(cause.getMessage(),cause);
    }
}
