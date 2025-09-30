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
 * 隧道通道处理器
 */
public class TunnelChannelHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {
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
        Channel visitorChannel = ctx.channel().attr(EtpConstants.NEXT_CHANNEL).get();
        //数据连接的断开
        if (visitorChannel!=null){
            String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
            Long sessionId = ctx.channel().attr(EtpConstants.SESSION_ID).get();
            //获取控制通道
            Channel controlTunnelChannel = ChannelManager.getControlTunnelChannel(secretKey);
            if (controlTunnelChannel!=null) {
                //删除该隧道所有的用户连接
                ChannelManager.removeVisitorChannelFromTunnelChannel(controlTunnelChannel, sessionId);
                ChannelUtils.closeOnFlush(controlTunnelChannel);
                visitorChannel.close();//关闭一个用户session连接
            }
        }else {
            //安全隧道的断开 相当于是客户端与服务器断开连接
            //1、删除该客户端的安全隧道绑定的所有公网端口号
            //2、清理掉所有绑定关系
            ChannelManager.removeTunnelAndBindRelationship(ctx.channel());
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel().attr(EtpConstants.NEXT_CHANNEL).get();
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
