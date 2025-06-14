package cn.xilio.vine.server.handler;

import cn.xilio.vine.core.MessageHandler;
import cn.xilio.vine.core.protocol.TunnelMessage;
import io.netty.channel.ChannelHandlerContext;
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
        System.out.println("断开连接了");

        //1、删除该客户端的安全隧道绑定的所有公网端口号

        //2、清理掉所有绑定关系

        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
