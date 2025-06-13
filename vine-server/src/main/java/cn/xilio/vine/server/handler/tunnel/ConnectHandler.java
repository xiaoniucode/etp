package cn.xilio.vine.server.handler.tunnel;

import cn.xilio.vine.core.Constants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.server.ChannelManager;
import cn.xilio.vine.core.AbstractMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;

/**
 * 连接消息处理器
 */
public class ConnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String ext = msg.getExt();
        String[] split = ext.split("@");
        String visitorId = split[0];
        String authToken = split[1];
        Channel tunnelChannel = ChannelManager.getTunnelChannel(authToken);
        Channel visitorChannel = ChannelManager.getVisitorChannel(tunnelChannel, visitorId);
        ctx.channel().attr(Constants.NEXT_CHANNEL).set(visitorChannel);
        visitorChannel.attr(Constants.NEXT_CHANNEL).set(ctx.channel());
        visitorChannel.config().setOption(ChannelOption.AUTO_READ, true);
    }
}
