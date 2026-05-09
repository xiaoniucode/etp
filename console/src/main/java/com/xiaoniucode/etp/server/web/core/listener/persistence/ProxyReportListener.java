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

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.enums.DomainType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.ProxyReportEvent;
import com.xiaoniucode.etp.server.service.repository.ProxyStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 代理配置创建事件处理，用于持久化代理客户端注册的代理配置信息
 */
@Component
public class ProxyReportListener implements EventListener<ProxyReportEvent> {
    private final Logger logger = LoggerFactory.getLogger(ProxyReportListener.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ProxyStore proxyStore;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onEvent(ProxyReportEvent event) {
        ProxyConfig config = event.getProxyConfig();
        AgentType agentType = config.getAgentType();
        ProtocolType protocol = config.getProtocol();
        boolean update = event.isUpdate();
        if (agentType.isEmbedded()) {
            if (protocol.isTcp()) {
                proxyStore.saveTcp(config);
            } else if (protocol.isHttp()) {
                Set<String> domains = event.getSubdomains();
                DomainType domainType = config.getRouteConfig().getDomainType();
                if (domainType.isCustomDomain()){
                    domains=config.getRouteConfig().getCustomDomains();
                }
                proxyStore.saveHttp(config, domains);
            }
        } else {

        }


        logger.debug("Received ProxyCreateEvent: {}", event);
    }
}
