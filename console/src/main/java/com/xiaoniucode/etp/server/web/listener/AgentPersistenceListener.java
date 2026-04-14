package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.AgentAuthEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
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
public class AgentPersistenceListener implements EventListener<AgentAuthEvent> {
    private final Logger logger = LoggerFactory.getLogger(AgentPersistenceListener.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private AgentRepository agentRepository;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(AgentAuthEvent event) {
        boolean reconnect = event.isReconnect();
        AgentInfo agentInfo = event.getAgentInfo();
        if (reconnect || agentInfo.getAgentType() == AgentType.SESSION) {
            return;
        }

        agentRepository.save(toAgentDTO(agentInfo));
        logger.info("客户端信息持久化成功: agentId={}, name={}, reconnect={}",
                agentInfo.getAgentId(), agentInfo.getName(), event.isReconnect());
    }

    private AgentDO toAgentDTO(AgentInfo agentInfo) {
        AgentDO agentDO = new AgentDO();
        agentDO.setId(agentInfo.getAgentId());
        agentDO.setName(agentInfo.getName());
        agentDO.setToken(agentInfo.getToken());
        agentDO.setAgentType(agentInfo.getAgentType());
        agentDO.setOs(agentInfo.getOs());
        agentDO.setArch(agentInfo.getArch());
        agentDO.setVersion(agentInfo.getVersion());
        agentDO.setLastActiveTime(agentInfo.getLastActiveTime());
        agentDO.setCreatedAt(agentInfo.getCreatedAt());
        return agentDO;
    }
}
