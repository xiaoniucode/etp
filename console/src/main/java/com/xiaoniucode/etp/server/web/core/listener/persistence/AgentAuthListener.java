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

package com.xiaoniucode.etp.server.web.core.listener.persistence;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.AgentAuthEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import com.xiaoniucode.etp.server.web.core.converter.AgentModelConvert;
import com.xiaoniucode.etp.server.service.repository.AgentStore;
import com.xiaoniucode.etp.server.web.entity.AgentDO;
import com.xiaoniucode.etp.server.web.repository.AgentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听代理客户端登陆事件，持久化客户端信息
 */
@Component
public class AgentAuthListener implements EventListener<AgentAuthEvent> {
    private final Logger logger = LoggerFactory.getLogger(AgentAuthListener.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentModelConvert agentModelConvert;
    @Autowired
    private AgentStore agentStore;
    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(AgentAuthEvent event) {
        logger.debug("Received AgentAuthEvent: {}", event);
        boolean reconnect = event.isReconnect();
        AgentInfo agentInfo = event.getAgentInfo();
        if (reconnect) {
            return;
        }
        if (agentInfo.getAgentType().isEmbedded()) {
            agentStore.save(agentInfo);

        }
        AgentDO agentDO = agentModelConvert.toDO(agentInfo);
        agentRepository.save(agentDO);
        logger.info("客户端信息保存成功: agentId={}, name={}, reconnect={}",
                agentInfo.getAgentId(), agentInfo.getName(), event.isReconnect());
    }
}
