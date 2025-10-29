package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.ChannelUtils;
import cn.xilio.etp.core.MessageHandler;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 负责认证、消息分发
 * @author liuxin
 */
public class ControlChannelHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        MessageHandler handler = MessageHandlerFactory.getHandler(msg.getType());
        handler.handle(ctx, msg);
    }

    /**
     * 服务端与某个客户端断开连接 可能是客户端们主动断开，也可能是代理服务器断开。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel clientChannel = ctx.channel().attr(EtpConstants.CLIENT_CHANNEL).get();
        //数据连接的断开
        if (clientChannel!=null){
            String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
            Long sessionId = ctx.channel().attr(EtpConstants.SESSION_ID).get();
            Channel controlTunnelChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
            if (controlTunnelChannel!=null) {
                ChannelManager.removeClientChannelFromControlChannel(controlTunnelChannel, sessionId);
                ChannelUtils.closeOnFlush(controlTunnelChannel);
                clientChannel.close();
            }
        }else {
            ChannelManager.clearControlChannel(ctx.channel());
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel().attr(EtpConstants.CLIENT_CHANNEL).get();
        if (visitorChannel!=null) {
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
