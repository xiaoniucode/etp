package com.xiaoniucode.etp.server.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用于统计通道流量和消息指标
 *
 * @author liuxin
 */
@ChannelHandler.Sharable
@Component
public class TrafficMetricsHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(TrafficMetricsHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel visitor = ctx.channel();
        if (!visitor.isActive()) {
            return;
        }
        MetricsCollector.doCollector(visitor, collector -> {
            collector.incReadMsgs(1);
            if (msg instanceof ByteBuf byteBuf) {
                collector.incReadBytes((byteBuf).readableBytes());
            }
        });

        super.channelRead(ctx, msg);
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Channel visitor = ctx.channel();
        if (!visitor.isActive()) {
            return;
        }
        MetricsCollector.doCollector(visitor, collector -> {
            collector.incWriteMsgs(1);
            if (msg instanceof ByteBuf byteBuf) {
                collector.incWriteBytes((byteBuf).readableBytes());
            }
        });

        super.write(ctx, msg, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        if (!visitor.isActive()) {
            return;
        }
        MetricsCollector.doCollector(visitor, MetricsCollector::incChannels);

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel visitor = ctx.channel();
        if (!visitor.isActive()) {
            return;
        }
        MetricsCollector.doCollector(visitor, MetricsCollector::decChannels);

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

