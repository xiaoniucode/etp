package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.core.AbstractMessageHandler;
import com.xiaoniucode.etp.core.ChannelUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.web.ConfigService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接断开，释放资源
 *
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
            logger.warn("control channel is null");
            return;
        }

        Channel visitorChannel = ChannelManager.removeClientChannelFromControlChannel(controlChannel, ctx.channel().attr(EtpConstants.SESSION_ID).get());
        if (visitorChannel != null) {
            ChannelUtils.closeOnFlush(visitorChannel);
            ctx.channel().attr(EtpConstants.DATA_CHANNEL).getAndSet(null);
            ctx.channel().attr(EtpConstants.SECRET_KEY).getAndSet(null);
            ctx.channel().attr(EtpConstants.SESSION_ID).getAndSet(null);
        }
        logger.debug("客户端断开连接");
    }
}
