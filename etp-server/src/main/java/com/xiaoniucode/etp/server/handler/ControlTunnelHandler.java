package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.ChannelUtils;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制隧道Channel处理器，负责消息分发、连接断开处理
 *
 * @author liuxin
 */
public class ControlTunnelHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {
    private final Logger logger = LoggerFactory.getLogger(ControlTunnelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        MessageHandler handler = TunnelMessageHandlerFactory.getHandler(msg.getType());
        if (handler != null) {
            handler.handle(ctx, msg);
        }
    }

    /**
     * 服务端与某个客户端断开连接 可能是客户端们主动断开，也可能是代理服务器断开。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel clientChannel = ctx.channel().attr(EtpConstants.VISITOR_CHANNEL).get();
        //数据连接的断开
        if (clientChannel != null) {
            String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
            Long sessionId = ctx.channel().attr(EtpConstants.SESSION_ID).get();
            Channel controlTunnelChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
            if (controlTunnelChannel != null) {
                ChannelManager.removeClientChannelFromControlChannel(controlTunnelChannel, sessionId);
                ChannelUtils.closeOnFlush(controlTunnelChannel);
                clientChannel.close();
            }
        } else {
            ChannelManager.clearControlChannel(ctx.channel());
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel().attr(EtpConstants.VISITOR_CHANNEL).get();
        if (visitorChannel != null) {
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        } else {
            logger.warn("channel wriability changed and visitorChannel is null");
        }

        super.channelWritabilityChanged(ctx);
    }
}
