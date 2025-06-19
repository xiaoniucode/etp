package cn.xilio.vine.server.handler.tunnel;

import cn.xilio.vine.core.AbstractMessageHandler;
import cn.xilio.vine.core.ChannelUtils;
import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.server.ChannelManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class DisconnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = ctx.channel().attr(VineConstants.SECRET_KEY).get();
        if (secretKey == null) {
            Long sessionId = msg.getSessionId();
            Channel userChannel = ChannelManager.removeVisitorChannelFromTunnelChannel(ctx.channel(), sessionId);
            if (userChannel != null) {
                ChannelUtils.closeOnFlush(userChannel);
            }
            return;
        }

        Channel tunnelChannel = ChannelManager.getTunnelChannel(secretKey);
        if (tunnelChannel == null) {
            return;
        }

        Channel visitorChannel = ChannelManager.removeVisitorChannelFromTunnelChannel(tunnelChannel, ctx.channel().attr(VineConstants.SESSION_ID).get());
        if (visitorChannel != null) {
            ChannelUtils.closeOnFlush(visitorChannel);
            ctx.channel().attr(VineConstants.NEXT_CHANNEL).remove();
            ctx.channel().attr(VineConstants.SECRET_KEY).remove();
            ctx.channel().attr(VineConstants.SESSION_ID).remove();
        }
    }
}
