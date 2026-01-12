package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.ChannelUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理客户端连接断开，释放资源
 *
 * @author liuxin
 */
public class CloseProxyHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(CloseProxyHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof CloseProxy closeProxy) {
            String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
            if (!StringUtils.hasText(secretKey)) {
                Long sessionId = closeProxy.getSessionId();
                Channel clientChannel = ChannelManager.removeClientChannelFromControlChannel(ctx.channel(), sessionId);
                if (clientChannel != null) {
                    ChannelUtils.closeOnFlush(clientChannel);
                }
                return;
            }

            Channel controlChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
            if (controlChannel == null) {
                logger.warn("控制隧道连接为空");
                return;
            }

            Channel visitorChannel = ChannelManager.removeClientChannelFromControlChannel(controlChannel, ctx.channel().attr(EtpConstants.SESSION_ID).get());
            if (visitorChannel != null) {
                ChannelUtils.closeOnFlush(visitorChannel);
                ctx.channel().attr(EtpConstants.DATA_CHANNEL).getAndSet(null);
                ctx.channel().attr(EtpConstants.SECRET_KEY).getAndSet(null);
                ctx.channel().attr(EtpConstants.SESSION_ID).getAndSet(null);
            }
            logger.debug("客户端:{} 断开连接", secretKey);
        }
    }
}
