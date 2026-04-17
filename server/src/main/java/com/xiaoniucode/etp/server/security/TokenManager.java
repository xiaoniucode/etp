package com.xiaoniucode.etp.server.security;

import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.AuthConfig;
import com.xiaoniucode.etp.server.config.domain.TokenConfig;
import com.xiaoniucode.etp.server.store.TokenStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final InternalLogger logger = InternalLoggerFactory.getInstance(TokenManager.class);

    /**
     * todo token --> 当前连接数
     */
    private final ConcurrentMap<String, AtomicInteger> connectionCountMap = new ConcurrentHashMap<>();

    /**
     * todo token --> 代理ID集合
     */
    private final ConcurrentMap<String, Set<String>> agentIdMap = new ConcurrentHashMap<>();

    @Resource
    private AppConfig appConfig;
    @Autowired
    private TokenStore tokenStore;

    /**
     * 初始化静态配置文件的令牌
     */
    @PostConstruct
    public void init() {
        AuthConfig authConfig = appConfig.getAuthConfig();
        List<TokenConfig> tokens = authConfig.getTokens();

        if (tokens != null && !tokens.isEmpty()) {
            for (TokenConfig tokenConfig : tokens) {
                addToken(tokenConfig);
            }
            logger.debug("初始化访问令牌完成，共加载 {} 个令牌", tokens.size());
        }
    }

    public boolean addToken(TokenConfig tokenConfig) {
        try {
            tokenStore.add(tokenConfig);
        } catch (Exception e) {
            logger.warn("Token 令牌添加失败: {}", e.getMessage());
            return false;
        }
        connectionCountMap.putIfAbsent(tokenConfig.getToken(), new AtomicInteger(0));
        agentIdMap.putIfAbsent(tokenConfig.getToken(), ConcurrentHashMap.newKeySet());
        logger.debug("添加访问令牌成功: {}", tokenConfig.getName());
        return true;
    }

    public void updateToken(TokenConfig tokenConfig) {
        tokenStore.update(tokenConfig);
    }

    /**
     * 获取令牌信息
     *
     * @param token 令牌
     * @return 令牌信息
     */
    public TokenConfig getByToken(String token) {
        return tokenStore.getByToken(token);
    }

    public boolean existsByName(String name) {
        return tokenStore.existsByName(name);
    }

    /**
     * 检查令牌是否存在
     *
     * @param token 令牌
     * @return 是否存在
     */
    public boolean existsByToken(String token) {
        return tokenStore.existsByToken(token);
    }

    /**
     * 移除访问令牌
     *
     * @param token 令牌
     */
    public void removeByToken(String token) {
        TokenConfig removedToken = tokenStore.deleteByToken(token);
        if (removedToken != null) {
            connectionCountMap.remove(token);
            agentIdMap.remove(token);
            logger.info("移除访问令牌: {}", token);
        }
    }

    public void removeByTokens(List<String> tokenList) {
        if (tokenList != null) {
            tokenList.forEach(this::removeByToken);
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

    public boolean checkAgentLimit(String token) {
        TokenConfig tokenConfig = getByToken(token);
        if (tokenConfig == null) {
            return false;
        }

        Set<String> agentIds = getOrInitializeAgentIds(token);
        if (tokenConfig.getMaxDevices() != TokenConfig.UNLIMITED_DEVICES) {
            return agentIds.size() < tokenConfig.getMaxDevices();
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
        TokenConfig tokenConfig = getByToken(token);
        if (tokenConfig == null) {
            return false;
        }

        AtomicInteger count = getOrInitializeConnectionCount(token);
        if (tokenConfig.getMaxConnections() != TokenConfig.UNLIMITED_CONNECTIONS) {
            return count.get() < tokenConfig.getMaxConnections();
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
            int newValue = count.updateAndGet(v -> Math.max(0, v - 1));
            logger.debug("令牌 {} 连接数减少到: {}", token, newValue);
        }
    }

    /**
     * 获取当前连接数
     *
     * @param token 令牌
     * @return 当前连接数
     */
    public int getOnlineConnectionCount(String token) {
        AtomicInteger count = connectionCountMap.get(token);
        return count != null ? count.get() : 0;
    }
}

