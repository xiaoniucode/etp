package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.server.manager.ChannelManager3;
import com.xiaoniucode.etp.server.manager.re.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 关闭某一个visitor连接
 *
 * @author liuxin
 */
public class CloseProxyHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(CloseProxyHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof CloseProxy closeProxy) {
            Channel control = ctx.channel();
            String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
            if (!StringUtils.hasText(secretKey)) {
                Long sessionId = closeProxy.getSessionId();

                Channel visitor = ChannelManager3.removeClientChannelFromControlChannel(control, sessionId);
                if (visitor != null) {
                    ChannelUtils.closeOnFlush(visitor);
                }
                return;
            }

            if (control == null) {
                logger.warn("控制隧道连接为空");
                return;
            }

            Channel visitor = ChannelManager3.removeClientChannelFromControlChannel(control, ctx.channel().attr(EtpConstants.SESSION_ID).get());
            if (visitor != null) {
                ChannelUtils.closeOnFlush(visitor);
                ctx.channel().attr(EtpConstants.DATA_CHANNEL).getAndSet(null);
                ctx.channel().attr(EtpConstants.SECRET_KEY).getAndSet(null);
                ctx.channel().attr(EtpConstants.SESSION_ID).getAndSet(null);
            }
            logger.debug("客户端:{} 断开连接", secretKey);
        }
    }
}
