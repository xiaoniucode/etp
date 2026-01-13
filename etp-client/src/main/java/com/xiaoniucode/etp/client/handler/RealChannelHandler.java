package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将从内网真实服务接收到的数据通过数据隧道发送到代理服务器
 *
 * @author liuxin
 */
public class RealChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(RealChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        Channel dataChannel = ctx.channel().attr(EtpConstants.DATA_CHANNEL).get();
        Long sessionId = ctx.channel().attr(EtpConstants.SESSION_ID).get();
        if (dataChannel == null) {
            logger.warn("数据传输通道为空:{}", sessionId);
            return;
        }
        dataChannel.writeAndFlush(new NewWorkConn(byteBuf.retain(),sessionId));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    /**
     * 该方法执行情况：1.公网连接者客户端主动断开连接. 2.公网客户端已经通过代理服务器连接上了，此时内网服务主动断开（如停机）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();
        Long sessionId = realChannel.attr(EtpConstants.SESSION_ID).get();
        logger.debug("session-id-{} 断开连接", sessionId);
        ChannelManager.removeRealServerChannel(sessionId);
        realChannel.attr(EtpConstants.SESSION_ID).set(null);

        Channel dataChannel = realChannel.attr(EtpConstants.DATA_CHANNEL).get();
        if (dataChannel != null) {
            dataChannel.attr(EtpConstants.REAL_SERVER_CHANNEL).set(null);
            realChannel.attr(EtpConstants.DATA_CHANNEL).set(null);
            dataChannel.writeAndFlush(new CloseProxy(sessionId));
        }
        super.channelInactive(ctx);
    }

    /**
     * 背压，流量控制
     * 当内网真实服务（realChannel）写缓冲区满,暂停数据隧道（dataTunnelChannel）的自动读取数据。
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realChannel = ctx.channel();
        Channel dataChannel = realChannel.attr(EtpConstants.DATA_CHANNEL).get();
        if (dataChannel != null) {
            dataChannel.config().setOption(ChannelOption.AUTO_READ, realChannel.isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
