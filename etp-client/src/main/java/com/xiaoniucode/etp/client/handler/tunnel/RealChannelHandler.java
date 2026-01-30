package com.xiaoniucode.etp.client.handler.tunnel;

import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.*;
/**
 *
 * @author liuxin
 */
public class RealChannelHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(RealChannelHandler.class);
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();
        String sessionId = realChannel.attr(EtpConstants.SESSION_ID).get();
        logger.debug("session-id-{} 断开连接", sessionId);
        ChannelManager.removeRealServerChannel(sessionId);
        realChannel.attr(EtpConstants.SESSION_ID).set(null);
        Channel control = ChannelManager.getControlChannel();
        Channel tunnel = realChannel.attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel != null) {
            tunnel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(null);
            realChannel.attr(EtpConstants.DATA_CHANNEL).set(null);
            MessageHeader header = MessageHeader.newBuilder().setType(MessageType.CLOSE_PROXY).build();

            Message.CloseProxy closeProxy = Message.CloseProxy.newBuilder().setSessionId(sessionId).build();
            ControlMessage message = ControlMessage.newBuilder().setHeader(header).setCloseProxy(closeProxy).build();
            control.writeAndFlush(message);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();
        Channel tunnel = realChannel.attr(EtpConstants.DATA_CHANNEL).get();
        if (tunnel != null) {
            tunnel.config().setOption(ChannelOption.AUTO_READ, realChannel.isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
