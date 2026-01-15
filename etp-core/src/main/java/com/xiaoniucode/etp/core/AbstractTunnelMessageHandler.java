package com.xiaoniucode.etp.core;

import com.xiaoniucode.etp.core.msg.Message;
import io.netty.channel.ChannelHandlerContext;


/**
 * 隧道消息处理器
 * @author liuxin
 */
public abstract class AbstractTunnelMessageHandler implements MessageHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, Message msg) throws Exception {
        try {
            // 检查当前通道是否激活
            if (!ctx.channel().isActive()) {
                return;
            }
            // 执行具体业务处理
            doHandle(ctx, msg);
        } catch (Exception e) {
            ctx.fireExceptionCaught(e);
            ctx.channel().close();
        }
    }

    protected abstract void doHandle(ChannelHandlerContext ctx, Message msg) throws Exception;
}
