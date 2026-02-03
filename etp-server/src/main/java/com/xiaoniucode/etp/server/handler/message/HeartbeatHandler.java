package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.handler.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.handler.utils.MessageWrapper;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.message.Message.ControlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 心跳消息处理
 *
 * @author liuxin
 */
@Component
public class HeartbeatHandler extends AbstractTunnelMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);
    @Autowired
    private AgentSessionManager agentSessionManager;

    @Override
    protected void doHandle(ChannelHandlerContext ctx, ControlMessage msg) {
        Channel control = ctx.channel();
        //更新代理客户端最后心跳时间
        agentSessionManager.updateHeartbeat(control);
        ControlMessage message = MessageWrapper.buildPong();
        control.writeAndFlush(message);
    }
}
