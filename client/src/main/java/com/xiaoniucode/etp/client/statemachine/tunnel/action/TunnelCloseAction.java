package com.xiaoniucode.etp.client.statemachine.tunnel.action;

import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelState;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelCloseAction extends TunnelBaseAction {
    private final Logger logger = LoggerFactory.getLogger(TunnelCloseAction.class);

    @Override
    protected void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        logger.debug("清理隧道资源: tunnelId={} TunnelType={}", context.getTunnelId(), context.getTunnelType().name());
        Channel tunnel = context.getTunnel();
        ChannelUtils.closeOnFlush(tunnel);
        TunnelManager.remove(context);
    }
}
