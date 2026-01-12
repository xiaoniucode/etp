package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.Ping;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 处理各种控制指令，分配给对应的处理器处理
 *
 * @author liuxin
 */
public class ControlChannelHandler extends SimpleChannelInboundHandler<Message> {
    private final Logger logger = LoggerFactory.getLogger(ControlChannelHandler.class);
    private final Consumer<ChannelHandlerContext> channelStatusCallback;

    public ControlChannelHandler(Consumer<ChannelHandlerContext> channelStatusCallback) {
        this.channelStatusCallback = channelStatusCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof Ping) {
            return;
        }
        MessageHandler handler = MessageHandlerFactory.getHandler(msg);
        if (handler != null) {
            handler.handle(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //控制通道断开
        if (ctx.channel() == ChannelManager.getControlChannel()) {
            logger.debug("与服务端断开连接");
            //清除当前控制通道
            ChannelManager.setControlChannel(null);
            ChannelManager.clearAllRealServerChannel();
            channelStatusCallback.accept(ctx);
        } else {
            //当前传输数据的通道断开
            ChannelManager.removeDataTunnelChanel(ctx.channel());
            Channel realChannel = ctx.channel().attr(EtpConstants.REAL_SERVER_CHANNEL).get();
            if (realChannel != null) {
                Long sessionId = realChannel.attr(EtpConstants.SESSION_ID).get();
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
