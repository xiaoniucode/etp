package com.xiaoniucode.etp.server.statemachine.tunnel;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

@Component
public class TunnelManager {

    public TunnelContext createTunnelContext(int connectionId, int tunnelId, Channel tunnel, boolean isMuxTunnel) {
        TunnelType tunnelType = isMuxTunnel ? TunnelType.MUX : TunnelType.DIRECT;
        TunnelContext context = TunnelContext.builder()
                .connectionId(connectionId)
                .tunnel(tunnel)
                .tunnelId(tunnelId)
                .tunnelType(tunnelType)
                .tunnelId(tunnelId)
                .state(TunnelState.IDLE)
                .build();
        return context;
    }
}
