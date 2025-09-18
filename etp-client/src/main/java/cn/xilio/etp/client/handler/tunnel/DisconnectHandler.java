package cn.xilio.etp.client.handler.tunnel;

import cn.xilio.etp.client.ChannelManager;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.ChannelUtils;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 *断开连接处理器
 */
public class DisconnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        Channel realChannel = ctx.channel().attr(EtpConstants.NEXT_CHANNEL).get();
        if (realChannel != null) {
            ctx.channel().attr(EtpConstants.NEXT_CHANNEL).remove();
            ChannelManager.returnDataTunnelChanel(ctx.channel());
            ChannelUtils.closeOnFlush(realChannel);
        }
    }
}
