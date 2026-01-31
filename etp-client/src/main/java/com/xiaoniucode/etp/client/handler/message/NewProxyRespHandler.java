package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.helper.ProxyRespHelper;
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
public class NewProxyRespHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewProxyRespHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        Message.NewProxyResp resp = msg.getNewProxyResp();
        ProxyRespHelper.set(resp);
        logger.info("公网访问地址: {}", resp.getRemoteAddr());
    }
}
