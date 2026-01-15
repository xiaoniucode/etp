package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.Error;
import com.xiaoniucode.etp.core.msg.Message;
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
    public void handle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof Error){
            Error error = (Error) msg;
            logger.error(error.getError());
        }
    }
}
