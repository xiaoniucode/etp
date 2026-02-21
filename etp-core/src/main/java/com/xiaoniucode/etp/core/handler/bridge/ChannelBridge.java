package com.xiaoniucode.etp.core.handler.bridge;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 桥接器，核心代码，用于实现两个channel双向透明转发。
 * [visitor <-> tunnel] <--> [tunnel <-> server]
 */
public class ChannelBridge extends ChannelDuplexHandler {
    private static final Logger log = LoggerFactory.getLogger(ChannelBridge.class);
    private final Channel peer;
    private final ChannelBridgeCallback callback;

    public ChannelBridge(Channel peer) {
        this(peer, null);
    }

    public ChannelBridge(Channel peer, ChannelBridgeCallback callback) {
        this.peer = peer;
        this.callback = callback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!peer.isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        ReferenceCountUtil.retain(msg);
        peer.writeAndFlush(msg).addListener((ChannelFutureListener) f -> {
            try {
                if (!f.isSuccess()) {
                    log.error("消息转发失败: {}", f.cause().getMessage());
                    closeOnFlush(ctx.channel());
                    closeOnFlush(peer);
                }
            } finally {
                if (ReferenceCountUtil.refCnt(msg) > 0) {
                    ReferenceCountUtil.release(msg);
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        closeOnFlush(peer);
        if (callback != null) {
            callback.onChannelInactive(ctx.channel(), peer);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.debug("bridge exception: {}", cause.getMessage());
        closeOnFlush(ctx.channel());
        closeOnFlush(peer);
        if (callback != null) {
            callback.onExceptionCaught(ctx.channel(), peer, cause);
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        boolean isWritable = ctx.channel().isWritable();
        if (callback != null) {
            callback.onChannelWritabilityChanged(ctx.channel(), peer, isWritable);
        }
    }

    public static void bridge(Channel a, Channel b, ChannelBridgeCallback callback) {
        a.pipeline().addLast(new ChannelBridge(b, callback)); //bridge1
        b.pipeline().addLast(new ChannelBridge(a, callback)); //bridge2
    }

    public static void bridge(Channel a, Channel b) {
        bridge(a, b, null);
    }

    private static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(ch.alloc().buffer(0)).addListener(ChannelFutureListener.CLOSE);
        }
    }
}

