package cn.xilio.etp.server.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;

/**
 * 用于统计通道流量和消息指标
 *
 * @author liuxin
 */
public class TrafficMetricsHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.incReadMsgs(1);
        collector.incReadBytes(((ByteBuf) msg).readableBytes());
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.incWriteMsgs(1);
        collector.incWriteBytes(((ByteBuf) msg).readableBytes());
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.incChannels();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.decChannels();
        super.channelInactive(ctx);
    }
}

