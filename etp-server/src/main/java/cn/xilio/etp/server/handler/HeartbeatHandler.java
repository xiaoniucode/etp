package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.protocol.TunnelMessage.Message;
import cn.xilio.etp.core.AbstractMessageHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 连接消息处理器
 *
 * @author liuxin
 */
public class HeartbeatHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        Message response = Message.newBuilder()
                .setType(Message.Type.HEARTBEAT)
                .build();
        ctx.channel().writeAndFlush(response);
    }
}
