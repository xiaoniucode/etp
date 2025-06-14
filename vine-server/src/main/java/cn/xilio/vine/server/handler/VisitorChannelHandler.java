package cn.xilio.vine.server.handler;

import cn.xilio.vine.core.VineConstants;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.server.ChannelManager;
import cn.xilio.vine.server.store.LocalServerInfo;
import cn.xilio.vine.server.store.ProxyManager;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

public class VisitorChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final AtomicLong sessionIdProducer = new AtomicLong(0);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        Channel tunnelChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
        try {
            buf.retain();
            ByteString byteString = ByteString.copyFrom(buf.nioBuffer());
            TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                    .setType(TunnelMessage.Message.Type.TRANSFER)
                    .setPayload(byteString)
                    .build();
            if (tunnelChannel.isWritable()) {
                tunnelChannel.writeAndFlush(message);
            }
        } finally {
            // 释放 ByteBuf
            ReferenceCountUtil.release(buf);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) visitorChannel.localAddress();
        Channel turnnelChannel = ChannelManager.getTunnelChannel(sa.getPort());
        if (turnnelChannel == null) {
            visitorChannel.close();
            return;
        } else {
            long nextSessionId = nextSessionId();
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, false);
            LocalServerInfo serverInfo = ProxyManager.getInstance().getInternalServerInfo(sa.getPort());
            ChannelManager.addVisitorChannelToTunnelChannel(visitorChannel, nextSessionId, turnnelChannel);
            TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                    .setType(TunnelMessage.Message.Type.CONNECT)
                    .setSessionId(nextSessionId)
                    .setPayload(ByteString.copyFrom((serverInfo.getLocalIp() + ":" + serverInfo.getLocalPort()).getBytes()))
                    .build();
            turnnelChannel.writeAndFlush(tunnelMessage);
        }
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // 当出现异常就关闭连接
        ctx.close();
    }

    public long nextSessionId() {
        return sessionIdProducer.incrementAndGet();
    }
}
