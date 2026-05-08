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

package com.xiaoniucode.etp.server.web.core.repository;

import com.xiaoniucode.etp.server.service.repository.AgentStore;
import com.xiaoniucode.etp.server.service.repository.AgentQueryRepository;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository("embeddedAgentQueryRepository")
public class EmbeddedAgentQueryRepository implements AgentQueryRepository, AgentStore {
    private final Map<String, AgentInfo> map = new ConcurrentHashMap<>();

    @Override
    public Optional<AgentInfo> findById(String agentId) {
        if (agentId == null) return Optional.empty();
        return Optional.ofNullable(map.get(agentId));
    }

    @Override
    public List<AgentInfo> findAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public void save(AgentInfo agentInfo) {
        map.put(agentInfo.getAgentId(), agentInfo);
    }

    @Override
    public void delete(String agentId) {
        if (agentId == null) return;
        map.remove(agentId);
    }
}
