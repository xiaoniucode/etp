package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.web.ConfigService;
import io.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下线代理信息
 *
 * @author liuxin
 */
public class ProxyUnregisterMessageHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ProxyUnregisterMessageHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, TunnelMessage.Message msg) {
        String proxyId = msg.getExt();
        String secretKey = ctx.channel().attr(EtpConstants.SECRET_KEY).get();
        JSONObject body = new JSONObject();
        body.put("id", Integer.parseInt(proxyId));
        body.put("secretKey", secretKey);
        ConfigService.deleteProxy(body);
        logger.info("代理：proxyId-{}下线", proxyId);
    }
}
