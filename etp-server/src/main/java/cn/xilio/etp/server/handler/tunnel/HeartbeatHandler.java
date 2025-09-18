package cn.xilio.etp.server.handler.tunnel;

import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.core.AbstractMessageHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 连接消息处理器
 */
public class HeartbeatHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        TunnelMessage.Message response = TunnelMessage.Message.newBuilder()
                .setType(TunnelMessage.Message.Type.HEARTBEAT)
                .build();
        ctx.channel().writeAndFlush(response);
    }
}
