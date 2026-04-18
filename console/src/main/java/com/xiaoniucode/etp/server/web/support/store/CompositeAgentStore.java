/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.support.store;

import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import com.xiaoniucode.etp.server.store.AgentStore;
import com.xiaoniucode.etp.server.web.entity.AgentDO;
import com.xiaoniucode.etp.server.web.repository.AgentRepository;
import com.xiaoniucode.etp.server.web.support.store.converter.AgentStoreConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class CompositeAgentStore implements AgentStore {
    private final Logger logger = LoggerFactory.getLogger(CompositeAgentStore.class);
    @Autowired
    private MultiLevelCache multiLevelCache;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentStoreConvert agentStoreConvert;
    private final String CACHE_NAME = "agent";

    @Override
    public void save(AgentInfo agentInfo) {
        logger.debug("保存代理信息，agentId: {}", agentInfo.getAgentId());
        multiLevelCache.evict(CACHE_NAME, "id:" + agentInfo.getAgentId());
    }

    @Override
    public AgentInfo findById(String agentId) {
        logger.debug("根据 agentId 查询代理信息，agentId: {}", agentId);
        return multiLevelCache.getAndPut(CACHE_NAME, "id:" + agentId, () -> {
            Optional<AgentDO> opt = agentRepository.findById(agentId);
            if (opt.isEmpty()) {
                return null;
            }
            return agentStoreConvert.toAgentInfo(opt.get());
        });
    }

    @Override
    public List<AgentInfo> findByToken(String token) {
        logger.debug("根据 token 查询代理列表，token: {}", token);
        String cacheKey = "token:" + token;
        return multiLevelCache.getAndPut(CACHE_NAME, cacheKey, () -> {
            List<AgentDO> agentDOs = agentRepository.findByToken(token);
            return agentStoreConvert.toAgentInfoList(agentDOs);
        });
    }

    @Override
    public long countByToken(String token) {
        logger.debug("根据 token 统计代理数量，token: {}", token);
        String cacheKey = "token:count:" + token;
        return multiLevelCache.getAndPut(CACHE_NAME, cacheKey, () ->
                agentRepository.countByToken(token)
        );
    }

    @Override
    public void delete(String agentId) {
        logger.debug("删除代理信息，agentId: {}", agentId);
        AgentInfo agentInfo = findById(agentId);
        if (agentInfo != null) {
            multiLevelCache.evict(CACHE_NAME, "id:" + agentId);
            multiLevelCache.evict(CACHE_NAME, "token:" + agentInfo.getToken());
            multiLevelCache.evict(CACHE_NAME, "token:count:" + agentInfo.getToken());
        } else {
            multiLevelCache.evict(CACHE_NAME, "id:" + agentId);
        }
    }

    @Override
    public int deleteExpiredOffline(String token, long timeout, ChronoUnit unit) {
        logger.debug("删除过期离线代理，token: {}, 超时时间: {} {}", token, timeout, unit);
        LocalDateTime expireTime = LocalDateTime.now().minus(timeout, unit);
        List<AgentDO> expiredAgents = agentRepository.findExpiredByToken(token, expireTime);
        logger.debug("查询到过期代理数量: {}", expiredAgents.size());

        int deletedCount = 0;
        for (AgentDO agentDO : expiredAgents) {
            multiLevelCache.evict(CACHE_NAME, "id:" + agentDO.getId());
            deletedCount++;
        }

        multiLevelCache.evict(CACHE_NAME, "token:" + token);
        multiLevelCache.evict(CACHE_NAME, "token:count:" + token);
        return deletedCount;
    }

    @Override
    public void updateLastActiveTime(String agentId, LocalDateTime lastActiveTime) {
        logger.debug("更新代理最后活跃时间，agentId: {}, 时间: {}", agentId, lastActiveTime);
        multiLevelCache.evict(CACHE_NAME, "id:" + agentId);

        multiLevelCache.getAndPut(CACHE_NAME, "id:" + agentId, () -> {
                    Optional<AgentDO> opt = agentRepository.findById(agentId);
                    if (opt.isEmpty()) {
                        return null;
                    }
                    AgentDO agentDO = opt.get();
                    AgentInfo agentInfo = agentStoreConvert.toAgentInfo(agentDO);
                    agentInfo.setLastActiveTime(lastActiveTime);
                    return agentInfo;
                }
        );
    }
}
