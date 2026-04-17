package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class InMemoryAgentStore implements AgentStore {

    private final Map<String, AgentInfo> store = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> tokenIndex = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock writeLock = rwLock.writeLock();

    @Override
    public void save(AgentInfo agentInfo) {
        writeLock.lock();
        try {
            String agentId = agentInfo.getAgentId();
            String token = agentInfo.getToken();

            store.put(agentId, agentInfo);

            tokenIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                    .add(agentId);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<AgentInfo> findById(String agentId) {
        return Optional.ofNullable(store.get(agentId));
    }

    @Override
    public List<AgentInfo> findByToken(String token) {
        Set<String> agentIds = tokenIndex.get(token);
        if (agentIds == null || agentIds.isEmpty()) {
            return Collections.emptyList();
        }

        return agentIds.stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public long countByToken(String token) {
        Set<String> agentIds = tokenIndex.get(token);
        return agentIds == null ? 0 : agentIds.size();
    }

    @Override
    public void delete(String agentId) {
        writeLock.lock();
        try {
            AgentInfo agent = store.remove(agentId);
            if (agent != null) {
                Set<String> agentIds = tokenIndex.get(agent.getToken());
                if (agentIds != null) {
                    agentIds.remove(agentId);
                    if (agentIds.isEmpty()) {
                        tokenIndex.remove(agent.getToken());
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 删除指定 token 下过期设备
     *
     * @param token   token
     * @param timeout 超时数值
     * @param unit    时间单位
     * @return 删除数量
     */
    @Override
    public int deleteExpiredOffline(String token, long timeout, ChronoUnit unit) {
        writeLock.lock();
        try {
            Set<String> agentIds = tokenIndex.get(token);
            if (agentIds == null || agentIds.isEmpty()) {
                return 0;
            }

            int deletedCount = 0;
            Iterator<String> iterator = agentIds.iterator();
            while (iterator.hasNext()) {
                String agentId = iterator.next();
                AgentInfo agent = store.get(agentId);

                if (agent != null && agent.isExpired(timeout, unit)) {
                    store.remove(agentId);
                    iterator.remove();
                    deletedCount++;
                }
            }

            if (agentIds.isEmpty()) {
                tokenIndex.remove(token);
            }

            return deletedCount;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void updateLastActiveTime(String agentId, LocalDateTime lastActiveTime) {
        writeLock.lock();
        try {
            Optional.ofNullable(store.get(agentId))
                    .ifPresent(agent -> agent.setLastActiveTime(lastActiveTime));
        } finally {
            writeLock.unlock();
        }
    }
}