package com.xiaoniucode.etp.client.handler.tunnel;

import com.xiaoniucode.etp.client.handler.utils.MessageWrapper;
import com.xiaoniucode.etp.client.manager.ServerSessionManager;
import com.xiaoniucode.etp.core.LanInfo;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author liuxin
 */
public class RealServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(RealServerHandler.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel server = ctx.channel();
        ServerSessionManager.removeServerSession(server).ifPresent(serverSession -> {
            Channel control = serverSession.getAgentSession().getControl();
            String sessionId = serverSession.getSessionId();
            LanInfo lanInfo = serverSession.getLanInfo();
            control.writeAndFlush(MessageWrapper.buildCloseProxy(sessionId));
            logger.debug("隧道关闭 - [会话标识={}，目标地址={}，目标端口={}]", sessionId, lanInfo.getLocalIP(), lanInfo.getLocalPort());
        });
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel server = ctx.channel();
        ServerSessionManager.getServerSession(server).ifPresent(session -> {
            Channel tunnel = session.getTunnel();
            tunnel.config().setOption(ChannelOption.AUTO_READ, server.isWritable());
        });
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
