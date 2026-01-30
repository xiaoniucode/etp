package com.xiaoniucode.etp.server.handler.message;

import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.ControlMessage;
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
        //更新agent 最后心跳时间
        agentSessionManager.updateHeartbeat(control);
        Message.MessageHeader header = Message.MessageHeader.newBuilder().setType(Message.MessageType.PONG).build();
        Message.Pong body = Message.Pong.newBuilder().build();
        ControlMessage message = ControlMessage.newBuilder().setHeader(header).setPong(body).build();
        control.writeAndFlush(message);
    }
}
