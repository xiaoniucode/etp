package com.xiaoniucode.etp.server.statemachine.tunnel;

import com.xiaoniucode.etp.core.statemachine.TunnelType;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 独立隧道连接池
 */
@Component
public class DirectTunnelPoolManager {
    private final Logger logger = LoggerFactory.getLogger(DirectTunnelPoolManager.class);
    private final Map<Integer, Map<String, TunnelContext>> clientToTunnels = new ConcurrentHashMap<>();
    private final Map<String, TunnelContext> tunnelIdToTunnels = new ConcurrentHashMap<>();

    public synchronized TunnelContext register(TunnelContext context) {
        context.setTunnelType(TunnelType.DIRECT);
        Map<String, TunnelContext> clientTunnels = clientToTunnels.computeIfAbsent(context.getConnectionId(), k -> new ConcurrentHashMap<>());
        clientTunnels.put(context.getTunnelId(), context);
        tunnelIdToTunnels.put(context.getTunnelId(), context);
        return context;

    }

    /**
     * 借用隧道连接
     */
    public TunnelContext borrow(String tunnelId) {
        TunnelContext context = tunnelIdToTunnels.get(tunnelId);
        Channel tunnel = context.getTunnel();
        if (tunnel != null && tunnel.isActive() && tunnel.isWritable()) {
            return context;
        }
        logger.info("隧道已失效，正在移除: connectionId={}, tunnelId={}", context.getConnectionId(), tunnelId);
        return null;
    }

}
