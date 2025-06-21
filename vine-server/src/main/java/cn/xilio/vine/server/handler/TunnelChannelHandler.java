package cn.xilio.vine.server.handler;

import cn.xilio.vine.core.ChannelUtils;
import cn.xilio.vine.core.MessageHandler;
import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.server.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

/**
 * 隧道通道处理器
 */
public class TunnelChannelHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {
    private final Logger logger= LoggerFactory.getLogger(TunnelChannelHandler.class);
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
        Channel visitorChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
        //数据连接的断开
        if (!ObjectUtils.isEmpty(visitorChannel)){
            String secretKey = ctx.channel().attr(VineConstants.SECRET_KEY).get();
            Long sessionId = ctx.channel().attr(VineConstants.SESSION_ID).get();
            //获取控制通道
            Channel controlTunnelChannel = ChannelManager.getControlTunnelChannel(secretKey);
            if (!ObjectUtils.isEmpty(controlTunnelChannel)) {
                //删除该隧道所有的用户连接
                ChannelManager.removeVisitorChannelFromTunnelChannel(controlTunnelChannel, sessionId);
                ChannelUtils.closeOnFlush(controlTunnelChannel);
                visitorChannel.close();//关闭一个用户session连接
            }
            logger.info("断开数据连接成功");
        }else {
            //安全隧道的断开 相当于是客户端与服务器断开连接
            //1、删除该客户端的安全隧道绑定的所有公网端口号
            //2、清理掉所有绑定关系
            ChannelManager.removeTunnelAndBindRelationship(ctx.channel());
            logger.info("断开隧道连接成功");
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
        if (!ObjectUtils.isEmpty(visitorChannel)) {
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
