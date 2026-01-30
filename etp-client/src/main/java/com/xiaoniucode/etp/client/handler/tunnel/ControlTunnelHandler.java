package com.xiaoniucode.etp.client.handler.tunnel;

import com.xiaoniucode.etp.client.ConnectionPool;
import com.xiaoniucode.etp.client.handler.factory.MessageHandlerFactory;
import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import com.xiaoniucode.etp.core.msg.Message.*;

/**
 *
 * @author liuxin
 */
@ChannelHandler.Sharable
public class ControlTunnelHandler extends SimpleChannelInboundHandler<ControlMessage> {
    private final Logger logger = LoggerFactory.getLogger(ControlTunnelHandler.class);
    private final Consumer<ChannelHandlerContext> channelStatusCallback;

    public ControlTunnelHandler(Consumer<ChannelHandlerContext> channelStatusCallback) {
        this.channelStatusCallback = channelStatusCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ControlMessage msg) throws Exception {
        MessageType type = msg.getHeader().getType();
        MessageHandler handler = MessageHandlerFactory.getHandler(type);
        if (handler != null) {
            handler.handle(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //控制通道断开
        if (ctx.channel() == ChannelManager.getControlChannel()) {
            logger.debug("断开与代理服务端的连接");
            //清除当前控制通道
            ChannelManager.setControlChannel(null);
            ChannelManager.clearAllRealServerChannel();
            channelStatusCallback.accept(ctx);
        } else {
            //当前传输数据的通道断开
            ConnectionPool.removeDataTunnelChanel(ctx.channel());
            Channel realChannel = ctx.channel().attr(EtpConstants.REAL_SERVER_CHANNEL).get();
            if (realChannel != null) {
                String sessionId = realChannel.attr(EtpConstants.SESSION_ID).get();
                logger.debug("session-id-{} 断开数据传输", sessionId);
                ctx.channel().close();
            }
        }

        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel().attr(EtpConstants.REAL_SERVER_CHANNEL).get();
        if (realChannel != null && realChannel.isActive()) {
            realChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }
}
