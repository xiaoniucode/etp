package cn.xilio.etp.server.handler;

import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.ChannelUtils;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuxin
 */
public class DisconnectHandler extends AbstractMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(DisconnectHandler.class);
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
        if (!StringUtils.hasText(secretKey)) {
            Long sessionId = msg.getSessionId();
            Channel clientChannel = ChannelManager.removeClientChannelFromControlChannel(ctx.channel(), sessionId);
            if (clientChannel != null) {
                ChannelUtils.closeOnFlush(clientChannel);
            }
            return;
        }

        Channel controlChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
        if (controlChannel == null) {
            return;
        }

        Channel clientChannel = ChannelManager.removeClientChannelFromControlChannel(controlChannel, ctx.channel().attr(EtpConstants.SESSION_ID).get());
        if (clientChannel != null) {
            ChannelUtils.closeOnFlush(clientChannel);
            ctx.channel().attr(EtpConstants.DATA_CHANNEL).getAndSet(null);
            ctx.channel().attr(EtpConstants.SECRET_KEY).getAndSet(null);
            ctx.channel().attr(EtpConstants.SESSION_ID).getAndSet(null);
        }
        logger.debug("客户端断开连接");
    }
}
