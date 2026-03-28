package com.xiaoniucode.etp.client.transport.connection;

import com.xiaoniucode.etp.client.common.UUIDGenerator;
import com.xiaoniucode.etp.core.transport.NettyBatchWriteQueue;
import com.xiaoniucode.etp.core.transport.PipelineConfigure;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import io.netty.channel.Channel;


public class MultiplexPool {
    private TunnelEntry tlsTunnelEntry;
    private TunnelEntry plainTunnelEntry;

    public TunnelEntry acquire(boolean isTls) {
        return isTls ? tlsTunnelEntry : plainTunnelEntry;
    }

    public TunnelEntry createChannel(boolean isTls, Channel tunnel) {
        String tunnelId = UUIDGenerator.generate();
        TunnelEntry tunnelEntry = new TunnelEntry(tunnelId,isTls, tunnel, NettyBatchWriteQueue.createWriteQueue(tunnel));
        if (isTls) {
            this.tlsTunnelEntry = tunnelEntry;
        } else {
            this.plainTunnelEntry = tunnelEntry;
        }
        return tunnelEntry;
    }

    public TunnelEntry activeTunnel(boolean isTls) {
        if (isTls) {
            tlsTunnelEntry.setActive(true);
            return tlsTunnelEntry;
        } else {
            plainTunnelEntry.setActive(true);
            return plainTunnelEntry;
        }
    }

    public void clearTunnel(boolean isTls) {
        if (isTls) {
            this.tlsTunnelEntry = null;
            return;
        }
        this.plainTunnelEntry = null;
    }
}
