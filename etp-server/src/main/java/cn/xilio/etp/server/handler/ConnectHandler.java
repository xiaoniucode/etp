package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.ChannelManager;
import cn.xilio.etp.core.AbstractMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;

/**
 * 连接消息处理器
 * @author liuxin
 */
public class ConnectHandler extends AbstractMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        long sessionId = msg.getSessionId();
        String secretKey = msg.getExt();
        Channel controlChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
        Channel clientChannel = ChannelManager.getClientChannel(controlChannel, sessionId);
        Channel dataChannel = ctx.channel();
        dataChannel.attr(EtpConstants.CLIENT_CHANNEL).set(clientChannel);
        dataChannel.attr(EtpConstants.SECRET_KEY).set(secretKey);
        dataChannel.attr(EtpConstants.SESSION_ID).set(sessionId);

        clientChannel.attr(EtpConstants.DATA_CHANNEL).set(ctx.channel());
        clientChannel.config().setOption(ChannelOption.AUTO_READ, true);
    }
}
