package com.xiaoniucode.etp.client.statemachine.tunnel;

import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DirectConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(DirectConnectionPool.class);
    private static final int MAX_TUNNEL_POOL_SIZE = 100;
    private static final Queue<TunnelContext> pool = new ConcurrentLinkedQueue<>();
    private DirectConnectionPool() {

    }

    public static boolean add(TunnelContext tunnelContext) {
        if (pool.size() > MAX_TUNNEL_POOL_SIZE) {
            return false;
        }
        return pool.add(tunnelContext);
    }

    public static Optional<TunnelContext> acquire() {
        if (pool.isEmpty()) {
            logger.error("没有可用连接");
            return Optional.empty();
        }
        while (!pool.isEmpty()) {
            TunnelContext context = pool.poll();
            if (context == null) {
                return Optional.empty();
            }
            if (!context.getTunnel().isActive()) {
                context.fireEvent(TunnelEvent.CLOSE);
            } else {
                return Optional.of(context);
            }
        }
        return Optional.empty();
    }

    public static void release(TunnelContext context) {
        if (pool.size() > MAX_TUNNEL_POOL_SIZE) {
            context.getTunnel().close();
        } else {
            context.getTunnel().config().setOption(ChannelOption.AUTO_READ, true);
            pool.offer(context);
        }
    }

    public static boolean remove(TunnelContext context) {
        return pool.remove(context);
    }
}
