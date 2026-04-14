package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.config.domain.TokenConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 本地内存存储，可外部存储，如Redis
 */
@Component
public class InMemoryTokenStore implements TokenStore {
    /**
     * token --> TokenConfig
     */
    private final Map<String, TokenConfig> tokenMap = new ConcurrentHashMap<>();
    /**
     * name --> TokenConfig
     */
    private final Map<String, TokenConfig> nameMap = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock writeLock = rwLock.writeLock();

    @Override
    public Optional<TokenConfig> findByToken(String token) {
        return Optional.ofNullable(tokenMap.get(token));
    }

    @Override
    public TokenConfig getByToken(String token) {
        return tokenMap.get(token);
    }

    @Override
    public List<TokenConfig> findAll() {
        return new ArrayList<>(tokenMap.values());
    }

    @Override
    public boolean existsByToken(String token) {
        return tokenMap.containsKey(token);
    }

    @Override
    public boolean existsByName(String name) {
        return nameMap.containsKey(name);
    }


    @Override
    public TokenConfig add(TokenConfig tokenConfig) {
        writeLock.lock();
        try {
            String name = tokenConfig.getName();
            if (nameMap.containsKey(name)) {
                throw new EtpException("令牌名称已存在: " + name);
            }
            if (tokenMap.containsKey(tokenConfig.getToken())) {
                throw new EtpException("令牌已存在: "+tokenConfig.getToken());
            }
            tokenMap.put(tokenConfig.getToken(), tokenConfig);
            nameMap.put(tokenConfig.getName(), tokenConfig);
            return tokenConfig;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public TokenConfig update(TokenConfig tokenConfig) {
        writeLock.lock();
        try {
            TokenConfig old = tokenMap.get(tokenConfig.getToken());
            if (old == null) {
                throw new EtpException("令牌不存在");
            }
            if (!old.getName().equals(tokenConfig.getName())) {
                // 检查新 name 是否冲突 排除自己
                TokenConfig conflict = nameMap.get(tokenConfig.getName());
                if (conflict != null && !conflict.getToken().equals(tokenConfig.getToken())) {
                    throw new EtpException("令牌名称已存在");
                }
                //删除旧的索引，添加新的索引
                nameMap.remove(old.getName());
                nameMap.put(tokenConfig.getName(), tokenConfig);
            }
            //更新令牌值索引
            tokenMap.put(tokenConfig.getToken(), tokenConfig);
            return tokenConfig;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public TokenConfig deleteByToken(String token) {
        TokenConfig removed = tokenMap.remove(token);
        if (removed != null) {
            nameMap.remove(removed.getName());
        }
        return removed;
    }
}