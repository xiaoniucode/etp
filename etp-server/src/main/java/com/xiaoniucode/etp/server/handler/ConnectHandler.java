package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.core.AbstractMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;

/**
 * 连接消息处理器
 *
 * @author liuxin
 */
public class ConnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        long sessionId = msg.getSessionId();
        String secretKey = msg.getExt();
        Channel controlChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
        if (controlChannel == null) {
            return;
        }
        Channel visitorChannel = ChannelManager.getClientChannel(controlChannel, sessionId);
        Channel dataChannel = ctx.channel();
        dataChannel.attr(EtpConstants.VISITOR_CHANNEL).set(visitorChannel);
        dataChannel.attr(EtpConstants.SECRET_KEY).set(secretKey);
        dataChannel.attr(EtpConstants.SESSION_ID).set(sessionId);

        visitorChannel.attr(EtpConstants.DATA_CHANNEL).set(ctx.channel());
        visitorChannel.config().setOption(ChannelOption.AUTO_READ, true);
    }
}
