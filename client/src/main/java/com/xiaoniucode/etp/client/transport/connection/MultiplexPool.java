package com.xiaoniucode.etp.client.transport.connection;

import com.xiaoniucode.etp.client.common.UUIDGenerator;
import com.xiaoniucode.etp.core.transport.NettyBatchWriteQueue;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


public class MultiplexPool {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(MultiplexPool.class);
    private TunnelEntry tlsTunnelEntry;
    private TunnelEntry plainTunnelEntry;

    public TunnelEntry acquire(boolean isTls) {
        TunnelEntry tunnelEntry = isTls ? tlsTunnelEntry : plainTunnelEntry;
        if (tunnelEntry == null) {
            return null;
        }
        Channel channel = tunnelEntry.getChannel();
        if (channel.isActive()) {
            return tunnelEntry;
        }
        logger.warn("多路复用连接已失效");
        clearTunnel(isTls);
        return null;
    }

    public TunnelEntry createChannel(boolean isTls, Channel tunnel) {
        if (!tunnel.isActive()) {
            return null;
        }
        String tunnelId = UUIDGenerator.generate();
        TunnelEntry tunnelEntry = new TunnelEntry(tunnelId, isTls, tunnel, NettyBatchWriteQueue.createWriteQueue(tunnel));
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
        logger.debug("清空多路复用连接");
        if (isTls && tlsTunnelEntry != null) {
            ChannelUtils.closeOnFlush(tlsTunnelEntry.getChannel());
            this.tlsTunnelEntry = null;
            return;
        }
        if (this.plainTunnelEntry != null) {
            ChannelUtils.closeOnFlush(plainTunnelEntry.getChannel());
            this.plainTunnelEntry = null;
        }
    }
}
