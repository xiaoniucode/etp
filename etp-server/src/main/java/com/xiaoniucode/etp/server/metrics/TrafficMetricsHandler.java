package com.xiaoniucode.etp.server.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 用于统计通道流量和消息指标
 *
 * @author liuxin
 */
public class TrafficMetricsHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(TrafficMetricsHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!ctx.channel().isActive()) {
            return;
        }
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.incReadMsgs(1);
        if (msg instanceof ByteBuf byteBuf) {
            collector.incReadBytes((byteBuf).readableBytes());
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!ctx.channel().isActive()) {
            return;
        }
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.incWriteMsgs(1);
        if (msg instanceof ByteBuf byteBuf) {
            collector.incWriteBytes((byteBuf).readableBytes());
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isActive()) {
            return;
        }
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.incChannels();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isActive()) {
            return;
        }
        InetSocketAddress sa = (InetSocketAddress) ctx.channel().localAddress();
        MetricsCollector collector = MetricsCollector.getCollector(sa.getPort());
        collector.decChannels();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!ctx.channel().isActive()) {
            return;
        }
        logger.warn("流量指标收集出错", cause);
        super.exceptionCaught(ctx, cause);
    }
}

