package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author liuxin
 */
public class RealChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(RealChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        Channel tunnel = ctx.channel().attr(EtpConstants.DATA_CHANNEL).get();
        Long sessionId = ctx.channel().attr(EtpConstants.SESSION_ID).get();
        if (tunnel == null) {
            logger.warn("数据传输通道为空:{}", sessionId);
            return;
        }
        tunnel.writeAndFlush(new NewWorkConn(byteBuf.retain(),sessionId));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

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
