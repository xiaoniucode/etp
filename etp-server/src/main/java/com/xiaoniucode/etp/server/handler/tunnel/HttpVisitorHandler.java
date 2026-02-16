package com.xiaoniucode.etp.server.handler.tunnel;

import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.server.handler.utils.MessageWrapper;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理来自公网访问者的请求
 */
@Component
@ChannelHandler.Sharable
public class HttpVisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(HttpVisitorHandler.class);
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel visitor = ctx.channel();
        Boolean connected = visitor.attr(ChannelConstants.CONNECTED).get();
        if (connected == null || !connected) {
            visitor.attr(ChannelConstants.CONNECTED).set(false);
            buf.retain();
            visitor.attr(ChannelConstants.HTTP_FIRST_PACKET).set(buf);
            visitor.config().setOption(ChannelOption.AUTO_READ, false);
            visitorSessionManager.registerVisitor(visitor, this::connectToTarget);
            ctx.pipeline().remove(this);
        }
    }

    private void connectToTarget(VisitorSession session) {
        Channel control = session.getControl();
        ProxyConfig config = session.getProxyConfig();
        Message.ControlMessage message = MessageWrapper
                .buildNewVisitorConn(session.getSessionId(),
                        config.getLocalIp(),
                        config.getLocalPort(),
                        config.getCompress(),
                        config.getEncrypt());
        control.writeAndFlush(message);
    }

    /**
     * 发送HTTP 协议的第一个数据包
     */
    public void sendFirstPackage(VisitorSession session) {
        Channel visitor = session.getVisitor();
        Channel tunnel = session.getTunnel();
        ByteBuf cached = visitor.attr(ChannelConstants.HTTP_FIRST_PACKET).get();
        if (cached != null && tunnel.isWritable()) {
            tunnel.writeAndFlush(cached.retain());
            cached.release();
            visitor.attr(ChannelConstants.HTTP_FIRST_PACKET).set(null);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitorSessionManager.disconnect(visitor, session -> {
            Channel tunnel = session.getTunnel();
            Message.ControlMessage message = MessageWrapper.buildCloseProxy(session.getSessionId());
            tunnel.writeAndFlush(message);
        });
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        VisitorSession visitorSession = visitorSessionManager.getVisitorSession(visitor);
        Channel tunnel = visitorSession.getTunnel();
        if (tunnel != null) {
            tunnel.config().setOption(ChannelOption.AUTO_READ, visitor.isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }
}
