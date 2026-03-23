package com.xiaoniucode.etp.server.transport.connection;

import com.xiaoniucode.etp.core.transport.TunnelEntry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 独立隧道连接池
 */
@Component
public class DirectPool {
    /**
     * clientId --> Pool
     */
    private final ConcurrentHashMap<String, Pool> clientPools = new ConcurrentHashMap<>();

    public TunnelEntry borrow(String clientId, String tunnelId) {
        Pool pool = clientPools.get(clientId);
        if (pool == null) {
            return null;
        }
        return pool.borrow(tunnelId);
    }

    public void release(String clientId, TunnelEntry tunnelEntry) {
        if (tunnelEntry == null || tunnelEntry.getTunnelId() == null) {
            return;
        }
        Pool pool = clientPools.get(clientId);
        if (pool == null) {
            return;
        }
        pool.release(tunnelEntry.getTunnelId(), tunnelEntry);
    }

    public void remove(String clientId, String tunnelId) {
        Pool pool = clientPools.get(clientId);
        if (pool != null) {
            pool.remove(tunnelId);
        }
    }

    public void register(String clientId, TunnelEntry tunnelEntry) {
        if (clientId == null || tunnelEntry == null || tunnelEntry.getTunnelId() == null) {
            return;
        }
        Pool pool = clientPools.computeIfAbsent(clientId, k -> new Pool());

        pool.register(tunnelEntry.getTunnelId(), tunnelEntry);
    }

    public void offline(String clientId) {
        Pool pool = clientPools.remove(clientId);
        if (pool != null) {
            pool.offline();
        }
    }

    static class Pool {
        /**
         * tunnelId --> PoolEntry
         */
        private final Map<String, TunnelEntry> pools = new ConcurrentHashMap<>();

        public TunnelEntry borrow(String tunnelId) {
            return pools.remove(tunnelId);
        }

        public void register(String tunnelId, TunnelEntry channel) {
            pools.putIfAbsent(tunnelId, channel);
        }

        public void remove(String tunnelId) {
            pools.remove(tunnelId);
        }

        public void release(String tunnelId, TunnelEntry channel) {
            if (pools.containsKey(tunnelId)) {
                pools.put(tunnelId, channel);
            }
        }

        public void offline() {
            pools.values().forEach(ch -> {
                if (ch != null && ch.getChannel().isActive()) {
                    ch.getChannel().close();
                }
            });
            pools.clear();
        }
    }

}
