package cn.xilio.etp.client.handler;

import cn.xilio.etp.client.ChannelManager;
import cn.xilio.etp.core.AbstractMessageHandler;
import cn.xilio.etp.core.ChannelUtils;
import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 断开连接处理器
 *
 * @author liuxin
 */
public class DisconnectHandler extends AbstractMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(DisconnectHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        Channel realChannel = ctx.channel().attr(EtpConstants.REAL_SERVER_CHANNEL).get();
        if (realChannel != null) {
            //归还数据通道到池中
            ChannelManager.returnDataTunnelChanel(ctx.channel());
            ChannelUtils.closeOnFlush(realChannel);
        } else {
            logger.debug("与内网真实服务器连接的channel为空！");
        }
    }
}
