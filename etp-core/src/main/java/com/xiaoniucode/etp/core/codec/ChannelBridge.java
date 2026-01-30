package com.xiaoniucode.etp.core.codec;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 桥接器，核心代码，用于实现两个channel双向透明转发。
 * [visitor <-> tunnel] <--> [tunnel <-> server]
 */
public class ChannelBridge extends ChannelDuplexHandler {
    private static final Logger log = LoggerFactory.getLogger(ChannelBridge.class);

    private final Channel peer;

    public ChannelBridge(Channel peer) {
        this.peer = peer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!peer.isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        ReferenceCountUtil.retain(msg);
        peer.writeAndFlush(msg).addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                log.debug("bridge write failed: {}", f.cause().getMessage());
                closeOnFlush(ctx.channel());
                closeOnFlush(peer);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 先传递给下一个handler
        closeOnFlush(peer);
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.debug("bridge exception: {}", cause.getMessage());
        closeOnFlush(ctx.channel());
        closeOnFlush(peer);
        ctx.fireExceptionCaught(cause);
    }

    public static void bridge(Channel a, Channel b) {
        a.pipeline().addLast(new ChannelBridge(b));
        b.pipeline().addLast(new ChannelBridge(a));
    }

    private static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(ch.alloc().buffer(0)).addListener(ChannelFutureListener.CLOSE);
        }
    }
}

