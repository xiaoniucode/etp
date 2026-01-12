package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将从公网代理服务器接受到的访问者发送的数据传输给内网真实目标服务
 *
 * @author liuxin
 */
public class NewWorkConnHandler extends AbstractTunnelMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(NewWorkConnHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message message) {
        if (message instanceof NewWorkConn) {
            NewWorkConn newWorkConn = (NewWorkConn) message;
            Channel realChannel = ctx.channel().attr(EtpConstants.REAL_SERVER_CHANNEL).get();
            if (realChannel != null) {
                realChannel.writeAndFlush(newWorkConn.getPayload().retain());
            } else {
                logger.warn("realChannel is null");
            }
        }
    }
}
