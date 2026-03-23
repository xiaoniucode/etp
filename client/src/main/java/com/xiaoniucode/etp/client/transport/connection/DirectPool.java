package com.xiaoniucode.etp.client.transport.connection;

import com.xiaoniucode.etp.client.common.UUIDGenerator;
import com.xiaoniucode.etp.core.transport.NettyBatchWriteQueue;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class DirectPool {
    private final Logger logger = LoggerFactory.getLogger(DirectPool.class);
    private static final int MAX_TUNNEL_POOL_SIZE = 100;
    private final Map<String, TunnelEntry> tunnels = new ConcurrentHashMap<>(10);

    public TunnelEntry borrow() {
        for (Map.Entry<String, TunnelEntry> mapEntry : tunnels.entrySet()) {
            TunnelEntry entry = mapEntry.getValue();
            if (entry.isActive()) {
                return tunnels.remove(mapEntry.getKey());
            } else {
                logger.warn("Tunnel {} is not active, removing from pool", entry.getTunnelId());
                closeTunnel(entry.getTunnelId());
            }
        }
        logger.warn("No active tunnels available in the pool");
        return null;
    }

    public TunnelEntry createTunnel(Channel channel) {
        if (tunnels.size() >= MAX_TUNNEL_POOL_SIZE) {
            return null;
        }
        String tunnelId = UUIDGenerator.generate();
        NettyBatchWriteQueue writeQueue = NettyBatchWriteQueue.createWriteQueue(channel);
        TunnelEntry tunnelEntry = new TunnelEntry(tunnelId, channel, writeQueue);
        tunnels.putIfAbsent(tunnelId, tunnelEntry);
        return tunnelEntry;
    }

    public TunnelEntry activateTunnel(String tunnelId) {
        TunnelEntry entry = tunnels.get(tunnelId);
        if (entry != null) {
            entry.setActive(true);
            return entry;
        }
        return null;
    }

    public void release(TunnelEntry entry) {
        if (entry == null) {
            return;
        }
        Channel tunnel = entry.getChannel();
        if (tunnels.size() > MAX_TUNNEL_POOL_SIZE) {
            tunnel.close();
            tunnels.remove(entry.getTunnelId());
        } else {
            tunnel.config().setOption(ChannelOption.AUTO_READ, true);
            tunnels.putIfAbsent(entry.getTunnelId(), entry);
        }
    }

    public void closeTunnel(String tunnelId) {
        TunnelEntry entry = tunnels.remove(tunnelId);
        if (entry != null) {
            Channel tunnel = entry.getChannel();
            if (tunnel != null && tunnel.isActive()) {
                tunnel.close();
            }
        }
    }
}
