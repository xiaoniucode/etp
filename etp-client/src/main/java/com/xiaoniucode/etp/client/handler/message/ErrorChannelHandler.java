package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.*;

/**
 *
 * @author liuxin
 */
public class ErrorChannelHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ErrorChannelHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        Message.Error error = msg.getError();
        logger.error("错误: {}", error.getMessage());
    }
}
