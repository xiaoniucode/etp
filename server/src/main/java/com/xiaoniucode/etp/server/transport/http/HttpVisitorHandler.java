package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.server.manager.domain.VisitorStream;
import com.xiaoniucode.etp.server.old.VisitorStreamManager;
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
    private VisitorStreamManager visitorStreamManager;



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel visitor = ctx.channel();
        Boolean connected = visitor.attr(ChannelConstants.CONNECTED).get();
//        if (connected == null || !connected) {
//            visitor.attr(ChannelConstants.CONNECTED).set(false);
//            buf.retain();
//            visitor.attr(ChannelConstants.HTTP_FIRST_PACKET).set(buf);
//            visitor.config().setOption(ChannelOption.AUTO_READ, false);
//            visitorStreamManager.createStream(visitor, visitorStream ->
//                    targetConnector.connectToTarget(ctx, visitorStream));
//
//
//            Channel control=null;
//            ProxyConfig config=null;
//            VisitorStreamContext streamContext = visitorManager.createStreamContext(visitor, control, config);
//            targetConnector.connectToTarget(streamContext);
//            ctx.pipeline().remove(this);
//        }
    }

    /**
     * 发送HTTP 协议的第一个数据包
     */
    public void sendFirstPackage(VisitorStream session) {
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
//        Channel visitor = ctx.channel();
//        visitorStreamManager.closeStream(visitor, session -> {
//            Channel tunnel = session.getTunnel();
//            tunnel.writeAndFlush(new TMSPFrame(session.getStreamId(), TMSP.MSG_CLOSE));
//        });
//        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        VisitorStream visitorSession = visitorStreamManager.getStream(visitor);
        Channel tunnel = visitorSession.getTunnel();
        if (tunnel != null) {
            tunnel.config().setOption(ChannelOption.AUTO_READ, visitor.isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }
}
