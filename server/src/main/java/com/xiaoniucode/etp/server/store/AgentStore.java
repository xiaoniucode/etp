package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public interface AgentStore {

    void save(AgentInfo agentInfo);

    Optional<AgentInfo> findById(String agentId);

    List<AgentInfo> findByToken(String token);

    long countByToken(String token);

    void delete(String agentId);

    int deleteExpiredOffline(String token, long timeout, ChronoUnit unit);

    void updateLastActiveTime(String agentId, LocalDateTime lastActiveTime);
}