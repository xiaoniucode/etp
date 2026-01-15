package com.xiaoniucode.etp.client.handler;

import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.client.TunnelClientHelper;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.msg.KickoutClient;
import com.xiaoniucode.etp.core.msg.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KickoutClientHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(KickoutClientHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof KickoutClient) {
            TunnelClient tunnelClient = TunnelClientHelper.getTunnelClient();
            if (tunnelClient != null) {
                tunnelClient.stop();
            }
            logger.info("客户端被强制下线！");
        }
    }
}
