package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.server.manager.ChannelManager;
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
            String secretKey = control.attr(EtpConstants.SECRET_KEY).get();
            ChannelManager.closeVisitor(secretKey, closeProxy.getSessionId());
            logger.debug("客户端:{} 断开连接", secretKey);
        }
    }
}
