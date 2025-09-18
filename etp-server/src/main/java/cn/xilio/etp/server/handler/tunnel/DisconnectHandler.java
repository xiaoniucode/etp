package cn.xilio.etp.server.handler.tunnel;

import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.ChannelUtils;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class DisconnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
        if (secretKey == null) {
            Long sessionId = msg.getSessionId();
            Channel visitorChannel = ChannelManager.removeVisitorChannelFromTunnelChannel(ctx.channel(), sessionId);
            if (visitorChannel != null) {
                ChannelUtils.closeOnFlush(visitorChannel);
            }
            return;
        }

        Channel controlTunnelChannel = ChannelManager.getControlTunnelChannel(secretKey);
        if (controlTunnelChannel == null) {
            return;
        }

        Channel visitorChannel = ChannelManager.removeVisitorChannelFromTunnelChannel(controlTunnelChannel, ctx.channel().attr(EtpConstants.SESSION_ID).get());
        if (visitorChannel != null) {
            ChannelUtils.closeOnFlush(visitorChannel);
            ctx.channel().attr(EtpConstants.NEXT_CHANNEL).remove();
            ctx.channel().attr(EtpConstants.SECRET_KEY).remove();
            ctx.channel().attr(EtpConstants.SESSION_ID).remove();
        }
    }
}
