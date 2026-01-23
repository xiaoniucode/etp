package com.xiaoniucode.etp.server.handler.visitor;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.LanInfo;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewVisitorConn;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class TcpVisitorHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(TcpVisitorHandler.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        ChannelManager.registerTcpVisitor(visitor, pair -> {
            Channel control = pair.getControl();
            //通知代理客户端与目标真实服务建立连接并建立传输隧道
            LanInfo lanInfo = pair.getLanInfo();
            control.writeAndFlush(new NewVisitorConn(pair.getSessionId(), lanInfo.getLocalIP(), lanInfo.getLocalPort()));
        });
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel();
        ChannelManager.unregisterTcpVisitor(visitorChannel, new BiConsumer<Long, Channel>() {
            @Override
            public void accept(Long sessionId, Channel tunnel) {
                //通知代理客户端连接断开
                tunnel.writeAndFlush(new CloseProxy(sessionId));
            }
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
        Channel control = ChannelManager.getControlByVisitor(visitor);
        if (control == null) {
            visitor.close();
        } else {
            Channel tunnel = visitor.attr(EtpConstants.DATA_CHANNEL).get();
            if (tunnel != null) {
                tunnel.config().setOption(ChannelOption.AUTO_READ, visitor.isWritable());
            }
        }
        super.channelWritabilityChanged(ctx);
    }
}
