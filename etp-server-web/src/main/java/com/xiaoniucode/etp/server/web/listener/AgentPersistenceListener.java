package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.AgentLoginEvent;
import com.xiaoniucode.etp.server.web.controller.client.request.ClientSaveRequest;
import com.xiaoniucode.etp.server.web.service.ClientService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 监听代理客户端登陆事件，持久化客户端信息
 */
@Component
public class AgentPersistenceListener implements EventListener<AgentLoginEvent> {
    private final Logger logger = LoggerFactory.getLogger(AgentPersistenceListener.class);

    @Autowired
    private EventBus eventBus;
    @Autowired
    private ClientService clientService;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    @Transactional
    public void onEvent(AgentLoginEvent event) {
        boolean isNew = event.isNew();
        ClientSaveRequest request = new ClientSaveRequest(
                event.getClientId(),
                event.getName(),
                event.getClientType(),
                event.getToken(),
                event.getArch(),
                event.getOs(),
                event.getVersion());
        clientService.saveClient(request);
        logger.debug("保存客户端：{}", request);
    }
}
