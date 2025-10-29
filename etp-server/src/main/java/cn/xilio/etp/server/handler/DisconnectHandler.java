package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.ChannelUtils;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author liuxin
 */
public class DisconnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
        if (secretKey == null) {
            Long sessionId = msg.getSessionId();
            Channel clientChannel = ChannelManager.removeVisitorChannelFromTunnelChannel(ctx.channel(), sessionId);
            if (clientChannel != null) {
                ChannelUtils.closeOnFlush(clientChannel);
            }
            return;
        }

        Channel controlChannel = ChannelManager.getControlTunnelChannel(secretKey);
        if (controlChannel == null) {
            return;
        }

        Channel clientChannel = ChannelManager.removeVisitorChannelFromTunnelChannel(controlChannel, ctx.channel().attr(EtpConstants.SESSION_ID).get());
        if (clientChannel != null) {
            ChannelUtils.closeOnFlush(clientChannel);
            ctx.channel().attr(EtpConstants.DATA_CHANNEL).getAndSet(null);//todo
            ctx.channel().attr(EtpConstants.SECRET_KEY).getAndSet(null);
            ctx.channel().attr(EtpConstants.SESSION_ID).getAndSet(null);
        }
    }
}
