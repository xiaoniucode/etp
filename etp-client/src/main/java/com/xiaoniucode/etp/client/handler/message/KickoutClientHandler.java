package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.client.helper.TunnelClientHelper;
import com.xiaoniucode.etp.core.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.msg.Message.*;

public class KickoutClientHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(KickoutClientHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        TunnelClient tunnelClient = TunnelClientHelper.getTunnelClient();
        if (tunnelClient != null) {
            tunnelClient.stop();
        }
        logger.info("客户端被强制下线！");
    }
}
