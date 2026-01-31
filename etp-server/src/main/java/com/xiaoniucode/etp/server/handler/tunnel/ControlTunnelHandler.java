package com.xiaoniucode.etp.server.handler.tunnel;

import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.server.handler.factory.TunnelMessageHandlerFactory;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ControlMessage msg) throws Exception {
        MessageType messageType = msg.getHeader().getType();
        MessageHandler handler = factory.getHandler(messageType);
        if (handler != null) {
            handler.handle(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel control = ctx.channel();
        agentSessionManager.disconnect(control);
        super.channelInactive(ctx);
    }
}
