package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.RuntimeState;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 处理来自公网客户端的连接读写请求
 *
 * @author liuxin
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);
    private static final AtomicLong SESSION_ID_PRODUCER = new AtomicLong(0);
    private final RuntimeState runtimeState = RuntimeState.get();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel dataChannel = ctx.channel().attr(EtpConstants.DATA_CHANNEL).get();
        ByteString payload = ByteString.copyFrom(buf.nioBuffer());
        TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                .setType(TunnelMessage.Message.Type.TRANSFER)
                .setPayload(payload)
                .build();
        if (dataChannel.isWritable()) {
            dataChannel.writeAndFlush(message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel clientChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) clientChannel.localAddress();
        Channel controllChannel = ChannelManager.getControlChannelByPort(sa.getPort());
        if (controllChannel == null) {
            clientChannel.close();
            return;
        } else {
            long nextSessionId = nextSessionId();
            clientChannel.config().setOption(ChannelOption.AUTO_READ, false);
            int localPort = runtimeState.getLocalPort(sa.getPort());
            ChannelManager.addClientChannelToControlChannel(clientChannel, nextSessionId, controllChannel);
            ChannelManager.registerActiveConnection(sa.getPort(), clientChannel);
            TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                    .setType(TunnelMessage.Message.Type.CONNECT)
                    .setSessionId(nextSessionId)
                    .setPort(localPort)
                    .build();
            controllChannel.writeAndFlush(message);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel clientChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) clientChannel.localAddress();
        Channel controlChannel = ChannelManager.getControlChannelByPort(sa.getPort());
        if (controlChannel == null) {
            ctx.channel().close();
        } else {
            Long sessionId = ChannelManager.getSessionIdByClientChannel(clientChannel);
            ChannelManager.removeClientChannelFromControlChannel(controlChannel, sessionId);
            ChannelManager.unregisterActiveConnection(sa.getPort(), clientChannel);
            Channel dataChannel = clientChannel.attr(EtpConstants.DATA_CHANNEL).get();
            if (dataChannel != null && dataChannel.isActive()) {
                dataChannel.attr(EtpConstants.REAL_SERVER_CHANNEL).getAndSet(null);
                dataChannel.attr(EtpConstants.SECRET_KEY).getAndSet(null);
                dataChannel.attr(EtpConstants.SESSION_ID).getAndSet(null);
                dataChannel.config().setOption(ChannelOption.AUTO_READ, true);
                TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                        .setType(TunnelMessage.Message.Type.DISCONNECT)
                        .setSessionId(sessionId)
                        .build();
                dataChannel.writeAndFlush(tunnelMessage);
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel clientChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) clientChannel.localAddress();
        Channel controlChannel = ChannelManager.getControlChannelByPort(sa.getPort());
        if (controlChannel == null) {
            ctx.channel().close();
        } else {
            Channel dataChannel = clientChannel.attr(EtpConstants.DATA_CHANNEL).get();
            if (dataChannel != null) {
                dataChannel.config().setOption(ChannelOption.AUTO_READ, clientChannel.isWritable());
            }
        }

        super.channelWritabilityChanged(ctx);
    }

    public long nextSessionId() {
        return SESSION_ID_PRODUCER.incrementAndGet();
    }
}
