package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.core.handler.MessageHandler;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.Message.*;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author liuxin
 */
public class ErrorChannelHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ErrorChannelHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        Message.Error error = msg.getError();
        int code = error.getCode();
        logger.error("{}", error.getMessage());
        if (code==401){
            System.exit(0);
        }
    }
}
