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

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 访问者通道处理器，用于处理来自外部访问者的请求（session），将访问者的消息通过数据隧道代理转发给内网
 */
public class VisitorChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final AtomicLong sessionIdProducer = new AtomicLong(0);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        //获取数据隧道-通道
        Channel dataTunnelChannel = ctx.channel().attr(VineConstants.NEXT_CHANNEL).get();
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
        Channel controllTurnnelChannel = ChannelManager.getControllTunnelChannel(sa.getPort());
        if (controllTurnnelChannel == null) {
            visitorChannel.close();
            return;
        } else {
            long nextSessionId = nextSessionId();
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, false);
            LocalServerInfo serverInfo = ProxyManager.getInstance().getInternalServerInfo(sa.getPort());
            ChannelManager.addVisitorChannelToTunnelChannel(visitorChannel, nextSessionId, controllTurnnelChannel);
            TunnelMessage.Message tunnelMessage = TunnelMessage.Message.newBuilder()
                    .setType(TunnelMessage.Message.Type.CONNECT)
                    .setSessionId(nextSessionId)
                    .setPayload(ByteString.copyFrom((serverInfo.getLocalIp() + ":" + serverInfo.getLocalPort()).getBytes()))
                    .build();
            controllTurnnelChannel.writeAndFlush(tunnelMessage);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 通知代理客户端
        Channel visitorChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) visitorChannel.localAddress();
        //获取控制通道
        Channel controllTunnelChannel = ChannelManager.getControllTunnelChannel(sa.getPort());
        if (controllTunnelChannel == null) {
            //该端口还没有配置代理规则，没有内网服务对应，断开的时候直接关闭通道就可以了
            ctx.channel().close();
        } else {
            //获取访问者的会话ID
            Long sessionId = ChannelManager.getVisitorChannelSessionId(visitorChannel);
            ChannelManager.removeVisitorChannelFromTunnelChannel(controllTunnelChannel, sessionId);
            //获取数据隧道通道
            Channel dataTunnelChannel = visitorChannel.attr(VineConstants.NEXT_CHANNEL).get();
            if (dataTunnelChannel != null && dataTunnelChannel.isActive()) {
                dataTunnelChannel.attr(VineConstants.NEXT_CHANNEL).remove();
                dataTunnelChannel.attr(VineConstants.SECRET_KEY).remove();
                dataTunnelChannel.attr(VineConstants.SESSION_ID).remove();

                dataTunnelChannel.config().setOption(ChannelOption.AUTO_READ, true);
                // 通知客户端，访问者连接已经断开
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
        cause.printStackTrace();
        // 当出现异常就关闭连接
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel visitorChannel = ctx.channel();
        InetSocketAddress sa = (InetSocketAddress) visitorChannel.localAddress();
        Channel controlleTunnelChannel = ChannelManager.getControllTunnelChannel(sa.getPort());
        if (controlleTunnelChannel == null) {
            ctx.channel().close();
        } else {
            Channel dataTunnelChannel = visitorChannel.attr(VineConstants.NEXT_CHANNEL).get();
            if (dataTunnelChannel != null) {
                dataTunnelChannel.config().setOption(ChannelOption.AUTO_READ, visitorChannel.isWritable());
            }
        }

        super.channelWritabilityChanged(ctx);
    }

    public long nextSessionId() {
        return sessionIdProducer.incrementAndGet();
    }
}
