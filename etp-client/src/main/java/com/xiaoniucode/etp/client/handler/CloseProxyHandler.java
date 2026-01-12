package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.ChannelManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.ChannelUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据传输隧道断开，归还连接到连接池，资源清理
 *
 * @author liuxin
 */
public class CloseProxyHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(CloseProxyHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        Channel realChannel = ctx.channel().attr(EtpConstants.REAL_SERVER_CHANNEL).get();
        if (realChannel != null) {
            ChannelManager.returnDataTunnelChanel(ctx.channel());
            ChannelUtils.closeOnFlush(realChannel);
        } else {
            logger.debug("与内网真实服务器连接的channel为空！");
        }
    }
}
