package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 错误处理器，处理来自服务端返回的错误消息
 *
 * @author liuxin
 */
public class ErrorChannelHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ErrorChannelHandler.class);

    @Override public void handle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String error = msg.getExt();
        logger.error(error);
    }
}
