package com.xiaoniucode.etp.server.statemachine.tunnel;

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
    /**
     * 一级索引 clientId
     * 二级索引 tunnelId
     */
    private final Map<String, Map<Integer, TunnelContext>> clientToTunnels = new ConcurrentHashMap<>();

    /**
     * tunnelId --> conn
     */
    private final Map<Integer, TunnelContext> tunnelIdToConnection = new ConcurrentHashMap<>();
    /**
     * tunnelId --> 是否被使用标记
     */
    private final Map<Integer, AtomicBoolean> inUseFlags = new ConcurrentHashMap<>();
    private final AtomicInteger auto=new AtomicInteger(0);

    public synchronized TunnelContext create(String clientId, Channel tunnel) {
        if (clientId == null || tunnel == null) {
            logger.warn("创建独立隧道失败：参数非法 clientId={}, tunnel={}", clientId, tunnel);
            return null;
        }
        int tunnelId = auto.getAndIncrement();
        TunnelContext conn = TunnelContext.builder()
                .clientId(clientId)
                .tunnelId(tunnelId)
                .tunnel(tunnel)
                .tunnelType(TunnelType.DIRECT).build();


        Map<Integer, TunnelContext> clientTunnels = clientToTunnels.computeIfAbsent(clientId, k -> new ConcurrentHashMap<>());
        TunnelContext old = clientTunnels.put(tunnelId, conn);


        if (old != null && old.getTunnel() != null && old.getTunnel().isActive()) {
            old.getTunnel().close();
            logger.debug("关闭旧独立隧道连接: clientId={}, tunnelId={}", clientId, old.getTunnelId());
        }

        logger.debug("创建独立隧道成功: clientId={}, tunnelId={}, channel={}", clientId, tunnelId, tunnel.remoteAddress());


        tunnel.closeFuture().addListener(fu -> {
            clientTunnels.remove(tunnelId);
            if (clientTunnels.isEmpty()) {
                clientToTunnels.remove(clientId);
            }
            logger.debug("独立隧道关闭并移除: clientId={}, tunnelId={}", clientId, tunnelId);
        });

        return conn;
    }

    /**
     * 借用隧道连接
     */
    public TunnelContext borrow(Integer tunnelId) {
        if (tunnelId == null) {
            logger.warn("借用隧道失败：tunnelId为空");
            return null;
        }

        TunnelContext conn = tunnelIdToConnection.get(tunnelId);
        if (conn == null) {
            logger.debug("隧道不存在: tunnelId={}", tunnelId);
            return null;
        }

        AtomicBoolean inUse = inUseFlags.get(tunnelId);
        if (inUse == null) {
            logger.debug("隧道标记不存在: tunnelId={}", tunnelId);
            return null;
        }

        // 尝试标记为已使用
        if (!inUse.compareAndSet(false, true)) {
            logger.debug("隧道正在使用中: tunnelId={}", tunnelId);
            return null;
        }

        Channel ch = conn.getTunnel();
        if (ch != null && ch.isActive() && ch.isWritable()) {
            logger.debug("借用隧道成功: tunnelId={}, clientId={}", tunnelId, conn.getClientId());
            return conn;
        }


        inUse.set(false);
        logger.info("隧道已失效，正在移除: clientId={}, tunnelId={}", conn.getClientId(), tunnelId);
        remove(conn.getClientId(), tunnelId);
        return null;
    }

    /**
     * 回收隧道连接
     */
    public boolean recycle(Integer tunnelId) {
        if (tunnelId == null) {
            logger.warn("回收隧道失败：tunnelId为空");
            return false;
        }

        AtomicBoolean inUse = inUseFlags.get(tunnelId);
        if (inUse == null) {
            logger.debug("回收失败：隧道标记不存在 tunnelId={}", tunnelId);
            return false;
        }

        TunnelContext conn = tunnelIdToConnection.get(tunnelId);
        if (conn == null) {
            logger.debug("回收失败：隧道不存在 tunnelId={}", tunnelId);
            inUseFlags.remove(tunnelId);
            return false;
        }

        Channel ch = conn.getTunnel();
        if (ch == null || !ch.isActive() || !ch.isWritable()) {
            logger.warn("回收的隧道已无效，将移除: tunnelId={}", tunnelId);
            remove(conn.getClientId(), tunnelId);
            return false;
        }


        inUse.set(false);
        logger.debug("回收隧道成功: tunnelId={}", tunnelId);
        return true;
    }

    /**
     * 手动移除并关闭隧道
     */
    public void remove(String clientId, Integer tunnelId) {
        Map<Integer, TunnelContext> clientTunnels = clientToTunnels.get(clientId);
        if (clientTunnels != null) {
            TunnelContext conn = clientTunnels.remove(tunnelId);
            if (clientTunnels.isEmpty()) {
                clientToTunnels.remove(clientId);
            }
            tunnelIdToConnection.remove(tunnelId);
            inUseFlags.remove(tunnelId);


            if (conn != null && conn.getTunnel() != null && conn.getTunnel().isActive()) {
                conn.getTunnel().close();
                logger.debug("手动关闭并移除隧道: clientId={}, tunnelId={}", clientId, tunnelId);
            }
        }
    }

    /**
     * 关闭某个客户端所有独立隧道
     */
    public void closeClient(String clientId) {
        Map<Integer, TunnelContext> clientTunnels = clientToTunnels.remove(clientId);
        if (clientTunnels != null) {
            clientTunnels.values().forEach(conn -> {
                Integer tunnelId = conn.getTunnelId();

                tunnelIdToConnection.remove(tunnelId);
                inUseFlags.remove(tunnelId);

                if (conn.getTunnel() != null && conn.getTunnel().isActive()) {
                    conn.getTunnel().close();
                }
            });
            logger.debug("关闭客户端所有隧道: clientId={}", clientId);
        }
    }

    /**
     * 根据tunnelId 获取隧道连接 不标记为占用
     */
    public TunnelContext get(Integer tunnelId) {
        return tunnelIdToConnection.get(tunnelId);
    }

    /**
     * 检查隧道是否在使用中
     */
    public boolean isInUse(Integer tunnelId) {
        AtomicBoolean inUse = inUseFlags.get(tunnelId);
        return inUse != null && inUse.get();
    }

    /**
     * 获取客户端隧道数量
     */
    public int getTunnelCount(String clientId) {
        Map<Integer, TunnelContext> clientTunnels = clientToTunnels.get(clientId);
        return clientTunnels == null ? 0 : clientTunnels.size();
    }

    /**
     * 总隧道数量
     */
    public int getTotalTunnelCount() {
        return tunnelIdToConnection.size();
    }

    /**
     * 检查隧道是否存在且有效
     */
    public boolean isValid(Integer tunnelId) {
        TunnelContext conn = tunnelIdToConnection.get(tunnelId);
        if (conn == null) {
            return false;
        }

        Channel ch = conn.getTunnel();
        return ch != null && ch.isActive() && ch.isWritable();
    }
}
