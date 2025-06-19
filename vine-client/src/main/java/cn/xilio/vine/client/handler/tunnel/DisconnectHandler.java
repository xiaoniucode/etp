package cn.xilio.vine.client.handler.tunnel;

import cn.xilio.vine.client.ChannelManager;
import cn.xilio.vine.core.AbstractMessageHandler;
import cn.xilio.vine.core.ChannelUtils;
import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 *断开连接处理器
 */
public class DisconnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        Channel realServerChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
        if (realServerChannel != null) {
            ctx.channel().attr(VineConstants.NEXT_CHANNEL).remove();
            ChannelManager.returnDataTunnelChanel(ctx.channel());
            ChannelUtils.closeOnFlush(realServerChannel);
        }
    }
}
