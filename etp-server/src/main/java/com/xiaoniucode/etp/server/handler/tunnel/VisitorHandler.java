package com.xiaoniucode.etp.server.handler.tunnel;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.LanInfo;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.server.manager.ProtocolDetection;
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
public class VisitorHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(VisitorHandler.class);
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        if (ProtocolDetection.isTcp(visitor)) {
            visitor.config().setOption(ChannelOption.AUTO_READ, false);
            visitorSessionManager.registerVisitor(visitor, this::connectToTarget);
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel visitor = ctx.channel();
        if (ProtocolDetection.isTcp(visitor)) {
            super.channelRead(ctx, msg);
            return;
        }
        if (msg instanceof ByteBuf buf){
            Boolean connected = visitor.attr(EtpConstants.CONNECTED).get();
            if (connected == null || !connected) {
                visitor.attr(EtpConstants.CONNECTED).set(false);
                buf.retain();
                visitor.attr(EtpConstants.HTTP_FIRST_PACKET).set(buf);
                visitor.config().setOption(ChannelOption.AUTO_READ, false);
                visitorSessionManager.registerVisitor(visitor, this::connectToTarget);
                return;
            }
            VisitorSession visitorSession = visitorSessionManager.getVisitorSession(visitor);
            Channel tunnel = visitorSession.getTunnel();
            if (tunnel == null || !tunnel.isActive()) {
                logger.warn("数据连接为空或未激活");
                return;
            }
            if (tunnel.isWritable()) {
                tunnel.writeAndFlush(buf.retain());
            }
        }

    }


    private void connectToTarget(VisitorSession session) {
        Channel control = session.getControl();
        LanInfo lanInfo = session.getLanInfo();
        Message.MessageHeader header = Message.MessageHeader.newBuilder()
                .setType(Message.MessageType.NEW_VISITOR).build();
        Message.NewVisitorConn newVisitorConn = Message.NewVisitorConn
                .newBuilder()
                .setSessionId(session.getSessionId())
                .setLocalIp(lanInfo.getLocalIP())
                .setLocalPort(lanInfo.getLocalPort())
                .build();
        Message.ControlMessage message = Message.ControlMessage.newBuilder()
                .setHeader(header)
                .setNewVisitorConn(newVisitorConn)
                .build();
        control.writeAndFlush(message);
    }

    /**
     * 发送HTTP 协议的第一个数据包
     */
    public void sendFirstPackage(VisitorSession session) {
        Channel visitor = session.getVisitor();
        Channel tunnel = visitor.attr(EtpConstants.DATA_CHANNEL).get();
        ByteBuf cached = visitor.attr(EtpConstants.HTTP_FIRST_PACKET).get();
        if (cached != null && tunnel.isWritable()) {
            tunnel.writeAndFlush(cached.retain());
            cached.release();
            visitor.attr(EtpConstants.HTTP_FIRST_PACKET).set(null);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitorSessionManager.disconnect(visitor, session -> {
            Channel tunnel = session.getTunnel();
            Message.MessageHeader header = Message.MessageHeader.newBuilder().setType(Message.MessageType.CLOSE_PROXY).build();

            Message.CloseProxy closeProxy = Message.CloseProxy
                    .newBuilder()
                    .setSessionId(session.getSessionId()).build();

            Message.ControlMessage message = Message.ControlMessage.newBuilder().setHeader(header).setCloseProxy(closeProxy).build();
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
        Channel tunnel = visitor.attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel != null) {
            tunnel.config().setOption(ChannelOption.AUTO_READ, visitor.isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }
}
