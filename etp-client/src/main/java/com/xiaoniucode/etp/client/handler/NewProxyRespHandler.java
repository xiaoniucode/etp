package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewProxyResp;
import io.netty.channel.Channel;
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
    public void handle(ChannelHandlerContext ctx, Message msg) {
        Channel controlChannel = ctx.channel();
        if (msg instanceof NewProxyResp) {
            NewProxyResp resp = (NewProxyResp) msg;
            //用于下线时通知服务端清理对应的资源
            controlChannel.attr(EtpConstants.PROXY_ID).set(resp.getProxyId());
            controlChannel.attr(EtpConstants.SESSION_ID).set(resp.getSessionId());
            String serverAddr = controlChannel.attr(EtpConstants.SERVER_DDR).get();
            logger.info("公网访问地址：{}:{}", serverAddr, resp.getRemoteAddr());
        }
    }
}
