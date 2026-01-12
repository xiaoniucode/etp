package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.Pong;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳消息处理
 *
 * @author liuxin
 */
public class HeartbeatHandler extends AbstractTunnelMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);
    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        ctx.channel().writeAndFlush(new Pong());
        logger.debug("连接心跳检查");
    }
}
