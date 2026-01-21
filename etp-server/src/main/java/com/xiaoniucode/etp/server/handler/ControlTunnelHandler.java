package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责消息分发、连接断开处理
 *
 * @author liuxin
 */
public class ControlTunnelHandler extends SimpleChannelInboundHandler<Message> {
    private final Logger logger = LoggerFactory.getLogger(ControlTunnelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        MessageHandler handler = TunnelMessageHandlerFactory.getHandler(msg);
        if (handler != null) {
            handler.handle(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel control = ctx.channel();
        Channel visitor = ctx.channel().attr(EtpConstants.VISITOR_CHANNEL).get();
        //数据连接的断开
        if (visitor != null) {
            String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
            Long sessionId = ctx.channel().attr(EtpConstants.SESSION_ID).get();
            ChannelManager.closeVisitor(secretKey, sessionId);
            ChannelUtils.closeOnFlush(control);
            visitor.close();
        }
        ChannelManager.closeControl(control);

        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel().attr(EtpConstants.VISITOR_CHANNEL).get();
        if (visitor != null) {
            visitor.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        } else {
            logger.warn("channel wriability changed and visitor is null");
        }

        super.channelWritabilityChanged(ctx);
    }
}
