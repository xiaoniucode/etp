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
public class TcpVisitorHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(TcpVisitorHandler.class);
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        visitorSessionManager.registerVisitor(visitor, this::connectToTarget);
       // ctx.pipeline().remove(this);
        super.channelActive(ctx);
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

    //todo 需要迁移改造通过桥接器实现
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

    //todo 需要迁移改造通过桥接器实现
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    //todo 需要迁移改造通过桥接器实现
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
