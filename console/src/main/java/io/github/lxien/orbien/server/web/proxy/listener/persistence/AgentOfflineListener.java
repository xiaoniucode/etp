/*
 *    Copyright 2026 lxien
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

package io.github.lxien.orbien.server.web.proxy.listener.persistence;

import io.github.lxien.orbien.core.enums.AgentType;
import io.github.lxien.orbien.server.event.AgentOfflineEvent;
import io.github.lxien.orbien.server.notify.EventBus;
import io.github.lxien.orbien.server.notify.EventListener;
import io.github.lxien.orbien.server.service.AgentConfigService;
import io.github.lxien.orbien.server.web.entity.AgentDO;
import io.github.lxien.orbien.server.web.repository.AgentRepository;
import io.github.lxien.orbien.server.web.service.ProxyService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class AgentOfflineListener implements EventListener<AgentOfflineEvent> {
    private final Logger logger = LoggerFactory.getLogger(AgentOfflineListener.class);

    @Autowired
    private EventBus eventBus;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentConfigService agentConfigService;
    @Autowired
    private ProxyService proxyService;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(AgentOfflineEvent event) {
        if (event == null || !StringUtils.hasText(event.getAgentId())) {
            return;
        }
        String agentId = event.getAgentId();
        AgentType agentType = resolveAgentType(event);
        if (agentType == null || !agentType.isSession()) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            try {
                cleanupSessionAgent(agentId);
            } catch (Exception e) {
                status.setRollbackOnly();
                logger.error("会话型客户端数据库记录清理失败: agentId={}", agentId, e);
            }
        });
    }

    private AgentType resolveAgentType(AgentOfflineEvent event) {
        if (event.getAgentType() != null) {
            return event.getAgentType();
        }
        return agentRepository.findById(event.getAgentId())
                .map(AgentDO::getAgentType)
                .orElse(null);
    }

    private void cleanupSessionAgent(String agentId) {
        // 即使 agent 行已被管理端删除，仍按 agentId 幂等清理残留代理
        proxyService.deleteByAgentIds(List.of(agentId));

        if (agentRepository.existsById(agentId)) {
            agentRepository.deleteById(agentId);
        }
        agentConfigService.evictById(agentId);
        logger.debug("会话型客户端数据库记录已清理: agentId={}", agentId);
    }
}
