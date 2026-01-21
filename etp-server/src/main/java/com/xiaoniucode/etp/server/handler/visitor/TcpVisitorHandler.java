package com.xiaoniucode.etp.server.handler.visitor;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewVisitorConn;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * 处理来自公网访问者连接读写请求
 *
 * @author liuxin
 */
public class TcpVisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(TcpVisitorHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel tunnel = ctx.channel().attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel == null || !tunnel.isActive()) {
            logger.warn("data channel is null");
            return;
        }
        if (tunnel.isWritable()) {
            tunnel.writeAndFlush(new NewWorkConn(buf.retain()));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        ChannelManager.registerTcpVisitor(visitor, pair -> {
            Channel control = pair.getControl();
            //通知代理客户端与目标端口建立连接
            control.writeAndFlush(new NewVisitorConn(pair.getSessionId(), pair.getLocalIP(), pair.getLocalPort()));
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
