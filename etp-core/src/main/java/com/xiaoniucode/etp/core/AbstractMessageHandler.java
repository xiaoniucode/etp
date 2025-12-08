package com.xiaoniucode.etp.core;

import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import io.netty.channel.ChannelHandlerContext;


/**
 * 抽象隧道消息处理器
 * @author liuxin
 */
public abstract class AbstractMessageHandler implements MessageHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception {
        try {
            // 通用前置检查
            if (!ctx.channel().isActive()) {
                return;
            }
            // 执行具体处理逻辑
            doHandle(ctx, msg);
        } catch (Exception e) {
            ctx.fireExceptionCaught(e);
            ctx.channel().close();
        }
    }

    protected abstract void doHandle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws Exception;
}
