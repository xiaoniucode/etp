package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.protocol.TunnelMessage.Message;
import com.xiaoniucode.etp.core.AbstractMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接消息处理器
 *
 * @author liuxin
 */
public class HeartbeatHandler extends AbstractMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);
    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        Message response = Message.newBuilder()
                .setType(Message.Type.HEARTBEAT)
                .build();
        ctx.channel().writeAndFlush(response);
        logger.debug("心跳检测");
    }
}
