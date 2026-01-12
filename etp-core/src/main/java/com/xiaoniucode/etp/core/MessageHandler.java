package com.xiaoniucode.etp.core;

import com.xiaoniucode.etp.core.msg.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * 隧道消息处理器接口
 * @author liuxin
 */
public interface MessageHandler {
    /**
     * 处理业务逻辑
     *
     * @param ctx channel上下文
     * @param msg 消息内容
     * @throws Exception 异常
     */
    void handle(ChannelHandlerContext ctx, Message msg) throws Exception;
}
