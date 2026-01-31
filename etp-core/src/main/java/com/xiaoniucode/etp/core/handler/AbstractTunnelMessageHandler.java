package com.xiaoniucode.etp.core.handler;

import io.netty.channel.ChannelHandlerContext;
import com.xiaoniucode.etp.core.message.Message.ControlMessage;

/**
 * 隧道消息处理器
 * @author liuxin
 */
public abstract class AbstractTunnelMessageHandler implements MessageHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) throws Exception {
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

    protected abstract void doHandle(ChannelHandlerContext ctx, ControlMessage msg) throws Exception;
}
