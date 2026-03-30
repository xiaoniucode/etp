package com.xiaoniucode.etp.server.security;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.TokenInfo;
import com.xiaoniucode.etp.server.store.TokenStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 访问令牌管理器
 * 负责管理访问令牌的生命周期、连接数限制和客户端ID管理
 */
@Component
public class TokenManager {
    private final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    /**
     * token --> 当前连接数
     */
    private final ConcurrentMap<String, AtomicInteger> connectionCountMap = new ConcurrentHashMap<>();

    /**
     * token --> 代理ID集合
     */
    private final ConcurrentMap<String, Set<String>> agentIdMap = new ConcurrentHashMap<>();

    @Resource
    private AppConfig appConfig;
    @Autowired
    private TokenStore tokenStore;
    @PostConstruct
    public void init() {
        List<TokenInfo> accessTokens = appConfig.getAccessTokens();
        if (accessTokens != null && !accessTokens.isEmpty()) {
            for (TokenInfo tokenInfo : accessTokens) {
                if (tokenStore.existsByToken(tokenInfo.getToken())) {
                    logger.warn("Token 令牌已经存在: {}", tokenInfo.getToken());
                    continue;
                }
                tokenStore.save(tokenInfo);
                connectionCountMap.putIfAbsent(tokenInfo.getToken(), new AtomicInteger(0));
                agentIdMap.putIfAbsent(tokenInfo.getToken(), ConcurrentHashMap.newKeySet());

                logger.debug("添加访问令牌: {}, 最大设备数: {}, 最大连接数: {}",
                        tokenInfo.getName(),
                        tokenInfo.getMaxDevices() == TokenInfo.UNLIMITED_DEVICES ? "无限制" : tokenInfo.getMaxDevices(),
                        tokenInfo.getMaxConnections() == TokenInfo.UNLIMITED_CONNECTIONS ? "无限制" : tokenInfo.getMaxConnections());
            }
            logger.debug("初始化访问令牌完成，共加载 {} 个令牌", accessTokens.size());
        }
    }
    /**
     * 获取或初始化连接数计数器
     *
     * @param token 令牌
     * @return 连接数计数器
     */
    private AtomicInteger getOrInitializeConnectionCount(String token) {
        AtomicInteger count = connectionCountMap.get(token);
        if (count == null) {
            count = connectionCountMap.putIfAbsent(token, new AtomicInteger(0));
            if (count == null) {
                count = connectionCountMap.get(token);
            }
        }
        return count;
    }

    /**
     * 获取或初始化代理ID集合
     *
     * @param token 令牌
     * @return 代理ID集合
     */
    private Set<String> getOrInitializeAgentIds(String token) {
        Set<String> agentIds = agentIdMap.get(token);
        if (agentIds == null) {
            agentIds = agentIdMap.putIfAbsent(token, ConcurrentHashMap.newKeySet());
            if (agentIds == null) {
                agentIds = agentIdMap.get(token);
            }
        }
        return agentIds;
    }

    /**
     * 获取令牌信息
     *
     * @param token 令牌
     * @return 令牌信息
     */
    public TokenInfo getAccessToken(String token) {
        return tokenStore.getByToken(token);
    }

    /**
     * 检查令牌是否存在
     *
     * @param token 令牌
     * @return 是否存在
     */
    public boolean checkToken(String token) {
        return tokenStore.existsByToken(token);
    }

    /**
     * 移除访问令牌
     *
     * @param token 令牌
     * @return 被移除的令牌信息
     */
    public Optional<TokenInfo> removeToken(String token) {
        TokenInfo removedToken = tokenStore.deleteByToken(token);
        if (removedToken != null) {
            connectionCountMap.remove(token);
            agentIdMap.remove(token);
            logger.info("移除访问令牌: {}", token);
        }
        return Optional.ofNullable(removedToken);
    }

    public boolean checkAgentLimit(String token) {
        TokenInfo tokenInfo = getAccessToken(token);
        if (tokenInfo == null) {
            return false;
        }

        Set<String> agentIds = getOrInitializeAgentIds(token);
        if (tokenInfo.getMaxDevices() != TokenInfo.UNLIMITED_DEVICES) {
            return agentIds.size() < tokenInfo.getMaxDevices();
        }
        return true;
    }
    /**
     * 检查连接数限制
     *
     * @param token 令牌
     * @return 是否允许连接
     */
    public boolean checkConnectionsLimit(String token) {
        TokenInfo tokenInfo = getAccessToken(token);
        if (tokenInfo == null) {
            return false;
        }
        
        AtomicInteger count = getOrInitializeConnectionCount(token);
        if (tokenInfo.getMaxConnections() != TokenInfo.UNLIMITED_CONNECTIONS) {
            return count.get() < tokenInfo.getMaxConnections();
        }
        return true;
    }

    /**
     * 增加连接数
     *
     * @param token 令牌
     */
    public void incrementConnection(String token) {
        AtomicInteger count = getOrInitializeConnectionCount(token);
        int newValue = count.incrementAndGet();
        logger.debug("令牌 {} 连接数增加到: {}", token, newValue);
    }

    /**
     * 减少连接数
     *
     * @param token 令牌
     */
    public void decrementConnection(String token) {
        AtomicInteger count = connectionCountMap.get(token);
        if (count != null) {
            int newValue = count.decrementAndGet();
            logger.debug("令牌 {} 连接数减少到: {}", token, newValue);
        }
    }
    /**
     * 获取当前连接数
     *
     * @param token 令牌
     * @return 当前连接数
     */
    public int getCurrentConnectionCount(String token) {
        AtomicInteger count = connectionCountMap.get(token);
        return count != null ? count.get() : 0;
    }

    /**
     * 获取当前代理ID数量
     *
     * @param token 令牌
     * @return 当前代理 ID数量
     */
    public int getAgentIdCount(String token) {
        Set<String> agentIds = agentIdMap.get(token);
        return agentIds != null ? agentIds.size() : 0;
    }

}

