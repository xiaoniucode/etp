package cn.xilio.vine.server.handler.tunnel;

import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.server.ChannelManager;
import cn.xilio.vine.core.AbstractMessageHandler;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;

/**
 * 认证消息处理器
 */
public class AuthHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = msg.getExt();
        ChannelManager.addTunnelChannel(3307, secretKey, ctx.channel());
    }
}
