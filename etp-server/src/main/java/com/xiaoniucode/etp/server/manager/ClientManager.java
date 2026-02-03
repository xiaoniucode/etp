package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端管理器
 * 负责客户端的注册、更新、移除和查询
 */
@Component
public class ClientManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

    /**
     * secretKey -> 客户端运行时信息
     */
    private static final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    /**
     * 注册客户端
     * secretKey不能为空，否则无法注册
     */
    public void addClient(ClientInfo client) {
        if (hasClient(client.getSecretKey())) {
            throw new RuntimeException("该客户端已经被注册:" + client.getName() + "-" + client.getSecretKey());
        }
        clients.putIfAbsent(client.getSecretKey(), client);
        logger.debug("客户端: {}-{} 注册成功", client.getName(), client.getSecretKey());
    }

    public void updateClientName(String secretKey, String name) {
        ClientInfo client = clients.get(secretKey);
        if (client != null) {
            client.setName(name);
        }
    }

    /**
     * 用于判断客户端是否存在与系统中，在认证授权的时候会用到
     *
     * @param secretKey 认证密钥
     * @return 是否存在
     */
    public boolean hasClient(String secretKey) {
        return clients.containsKey(secretKey);
    }

    /**
     * 下线客户端
     */
    public void removeClient(String secretKey) {
        clients.remove(secretKey);
    }

    /**
     * 通过客户端密钥获取客户端信息
     */
    public ClientInfo getClient(String secretKey) {
        return clients.get(secretKey);
    }

    /**
     * 获取所有客户端信息
     */
    public Collection<ClientInfo> allClients() {
        return clients.values();
    }

    public int getClientCount() {
        return clients.size();
    }
}