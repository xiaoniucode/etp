package cn.xilio.etp.client.handler.tunnel;

import cn.xilio.etp.client.ChannelManager;
import cn.xilio.etp.core.ChannelStatusCallback;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.MessageHandler;
import cn.xilio.etp.core.protocol.TunnelMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.springframework.util.ObjectUtils;

public class TunnelChannelHandler extends SimpleChannelInboundHandler<TunnelMessage.Message> {
    private final Bootstrap realBootstrap;
    private final Bootstrap tunnelBootstrap;
    /**
     * 连接断开的时候回调接口
     */
    private final ChannelStatusCallback channelStatusCallback;

    public TunnelChannelHandler(Bootstrap realBootstrap, Bootstrap tunnelBootstrap, ChannelStatusCallback channelStatusCallback) {
        this.realBootstrap = realBootstrap;
        this.tunnelBootstrap = tunnelBootstrap;
        this.channelStatusCallback = channelStatusCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        if (TunnelMessage.Message.Type.HEARTBEAT.getNumber() == msg.getType().getNumber()) {
            return;//客户端不处理心跳
        }
        MessageHandler handler = MessageHandlerFactory.getHandler(msg.getType());
        ctx.channel().attr(EtpConstants.REAL_BOOTSTRAP).set(realBootstrap);
        ctx.channel().attr(EtpConstants.TUNNEL_BOOTSTRAP).set(tunnelBootstrap);
        handler.handle(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //控制通道断开 因为与代理服务器的连接会存在一条控制通道和多条数据通道
        if (ctx.channel() == ChannelManager.getControlTunnelChannel()) {
            //清除当前控制通道
            ChannelManager.setControlTunnelChannel(null);
            ChannelManager.clearAllRealServerChannel();
            //控制通道断开回调
            channelStatusCallback.channelInactive(ctx);
        } else {
            //数据隧道-通道断开连接 此时需要关闭代理客户端与真实服务的连接
            Channel realServerChannel = ctx.channel().attr(EtpConstants.NEXT_CHANNEL).get();
            if (!ObjectUtils.isEmpty(realServerChannel)) {
                ctx.channel().close();
            }
        }
        //从数据隧道池中删除该通道
        ChannelManager.removeDataTunnelChanel(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel().attr(EtpConstants.NEXT_CHANNEL).get();
        if (!ObjectUtils.isEmpty(realChannel) && realChannel.isActive()) {
            realChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }
}
