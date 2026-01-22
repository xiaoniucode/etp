package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author liuxin
 */
public class RealChannelHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(RealChannelHandler.class);
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();
        Long sessionId = realChannel.attr(EtpConstants.SESSION_ID).get();
        logger.debug("session-id-{} 断开连接", sessionId);
        ChannelManager.removeRealServerChannel(sessionId);
        realChannel.attr(EtpConstants.SESSION_ID).set(null);

        Channel tunnel = realChannel.attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel != null) {
            tunnel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(null);
            realChannel.attr(EtpConstants.DATA_CHANNEL).set(null);
            tunnel.writeAndFlush(new CloseProxy(sessionId));
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();
        Channel tunnel = realChannel.attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel != null) {
            tunnel.config().setOption(ChannelOption.AUTO_READ, realChannel.isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
