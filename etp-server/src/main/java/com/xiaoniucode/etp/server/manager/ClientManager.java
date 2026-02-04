package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理客户端管理器
 */
@Component
public class ClientManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

    /**
     * clientId -> ClientInfo
     * 已连接的客户端映射
     */
    private static final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    /**
     * 添加客户端
     */
    public boolean addClient(ClientInfo clientInfo) {
        if (hasClient(clientInfo.getClientId())) {
            logger.warn("客户端添加失败：客户端已经存在");
            return false;
        }
        clients.putIfAbsent(clientInfo.getClientId(), clientInfo);
        logger.debug("客户端添加成功: [客户端标识={}，名称={}]", clientInfo.getClientId(), clientInfo.getName());
        return true;
    }

    /**
     * 检查客户端是否存在
     */
    public boolean hasClient(String clientId) {
        return clients.containsKey(clientId);
    }

    /**
     * 移除客户端
     */
    public void removeClient(String clientId) {
        clients.remove(clientId);
        logger.debug("客户端已移除: {}", clientId);
    }

    /**
     * 获取客户端信息
     */
    public ClientInfo getClient(String clientId) {
        return clients.get(clientId);
    }

    /**
     * 获取所有客户端
     */
    public Collection<ClientInfo> getAllClients() {
        return clients.values();
    }

    /**
     * 获取客户端数量
     */
    public int getClientCount() {
        return clients.size();
    }

    /**
     * 清空所有客户端
     */
    public void clearAllClients() {
        clients.clear();
        logger.debug("已清空所有客户端");
    }

    /**
     * 获取客户端ID列表
     */
    public List<String> getClientIds() {
        return new ArrayList<>(clients.keySet());
    }
}