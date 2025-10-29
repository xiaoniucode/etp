package cn.xilio.etp.server.handler;

import cn.xilio.etp.core.EtpConstants;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.ChannelManager;
import cn.xilio.etp.server.store.Config;
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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        Channel dataTunnelChannel = ctx.channel().attr(EtpConstants.DATA_CHANNEL).get();
        ByteString payload = ByteString.copyFrom(buf.nioBuffer());
        TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                .setType(TunnelMessage.Message.Type.TRANSFER)
                .setPayload(payload)
                .build();
        if (dataTunnelChannel.isWritable()) {
            dataTunnelChannel.writeAndFlush(message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) visitorChannel.localAddress();
        Channel controllTurnnelChannel = ChannelManager.getControlTunnelChannel(sa.getPort());
        if (controllTurnnelChannel == null) {
            visitorChannel.close();
            return;
        } else {
            long nextSessionId = nextSessionId();
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, false);
            Integer localPort = Config.getInstance().getInternalServerInfo(sa.getPort());

            ChannelManager.addVisitorChannelToTunnelChannel(visitorChannel, nextSessionId, controllTurnnelChannel);

            TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                    .setType(TunnelMessage.Message.Type.CONNECT)
                    .setSessionId(nextSessionId)
                    .setPort(localPort)
                    .build();
            controllTurnnelChannel.writeAndFlush(tunnelMessage);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) visitorChannel.localAddress();
        Channel controllTunnelChannel = ChannelManager.getControlTunnelChannel(sa.getPort());
        if (controllTunnelChannel == null) {
            ctx.channel().close();
        } else {
            Long sessionId = ChannelManager.getVisitorChannelSessionId(visitorChannel);
            ChannelManager.removeVisitorChannelFromTunnelChannel(controllTunnelChannel, sessionId);
            Channel dataTunnelChannel = visitorChannel.attr(EtpConstants.DATA_CHANNEL).get();
            if (dataTunnelChannel != null && dataTunnelChannel.isActive()) {
                dataTunnelChannel.attr(EtpConstants.REAL_SERVER_CHANNEL).remove();
                dataTunnelChannel.attr(EtpConstants.SECRET_KEY).remove();
                dataTunnelChannel.attr(EtpConstants.SESSION_ID).remove();

                dataTunnelChannel.config().setOption(ChannelOption.AUTO_READ, true);
                TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                        .setType(TunnelMessage.Message.Type.DISCONNECT)
                        .setSessionId(sessionId)
                        .build();
                dataTunnelChannel.writeAndFlush(tunnelMessage);
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
        Channel visitorChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) visitorChannel.localAddress();
        Channel controlTunnelChannel = ChannelManager.getControlTunnelChannel(sa.getPort());
        if (controlTunnelChannel == null) {
            ctx.channel().close();
        } else {
            Channel dataTunnelChannel = visitorChannel.attr(EtpConstants.DATA_CHANNEL).get();
            if (dataTunnelChannel != null) {
                dataTunnelChannel.config().setOption(ChannelOption.AUTO_READ, visitorChannel.isWritable());
            }
        }

        super.channelWritabilityChanged(ctx);
    }

    public long nextSessionId() {
        return SESSION_ID_PRODUCER.incrementAndGet();
    }
}
