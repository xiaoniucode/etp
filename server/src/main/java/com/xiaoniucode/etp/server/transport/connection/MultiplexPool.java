package com.xiaoniucode.etp.server.transport.connection;

import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiplexPool {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MultiplexPool.class);

    /**
     * clientId --> Pool
     */
    private final ConcurrentHashMap<String, Pool> clientPools = new ConcurrentHashMap<>();


    public TunnelEntry acquire(String clientId, boolean isTls) {
        Pool pool = clientPools.get(clientId);
        if (pool == null) {
            logger.warn("客户端 {} 连接池为空，没有可用连接", clientId);
            return null;
        }
        return pool.acquire(isTls);
    }

    public void setChannel(String clientId, boolean isTls, TunnelEntry entry) {
        if (clientId == null || entry == null) {
            throw new IllegalArgumentException("客户端 ID或隧道节点不能为空");
        }
        Pool pool = clientPools.computeIfAbsent(clientId, k -> new Pool());
        pool.setChannel(isTls, entry);
    }

    public void offline(String clientId) {
        if (clientId == null) {
            return;
        }
        Pool pool = clientPools.remove(clientId);
        if (pool != null) {
            pool.offline();
        }
    }

    static class Pool {
        protected TunnelEntry plainEntry;
        protected TunnelEntry tlsEntry;

        public TunnelEntry acquire(boolean isTls) {
            return isTls ? tlsEntry : plainEntry;
        }

        public void setChannel(boolean isTls, TunnelEntry entry) {
            if (isTls) {
                this.tlsEntry = entry;
            } else {
                this.plainEntry = entry;
            }
        }

        public void offline() {
            if (plainEntry != null) {
                ChannelUtils.closeOnFlush(plainEntry.getChannel());
            }
            if (tlsEntry != null) {
                ChannelUtils.closeOnFlush(tlsEntry.getChannel());
            }
        }
    }

}
