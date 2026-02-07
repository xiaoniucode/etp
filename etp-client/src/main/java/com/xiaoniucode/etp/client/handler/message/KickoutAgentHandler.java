package com.xiaoniucode.etp.client.handler.message;

import com.xiaoniucode.etp.client.TunnelClient;
import com.xiaoniucode.etp.client.helper.TunnelClientHelper;
import com.xiaoniucode.etp.client.manager.AgentSessionManager;
import com.xiaoniucode.etp.core.handler.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xiaoniucode.etp.core.message.Message.*;

public class KickoutAgentHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(KickoutAgentHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, ControlMessage msg) {
        AgentSessionManager.getAgentSession().ifPresent(agent -> {
            logger.info("客户端被强制下线]");
        });
        TunnelClient tunnelClient = TunnelClientHelper.getTunnelClient();
        if (tunnelClient != null) {
            tunnelClient.stop();
        }
    }
}
