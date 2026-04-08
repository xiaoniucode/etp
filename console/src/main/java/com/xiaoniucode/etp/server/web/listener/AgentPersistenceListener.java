package com.xiaoniucode.etp.server.web.listener;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.AgentAuthEvent;
import com.xiaoniucode.etp.server.web.service.AgentService;
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
public class AgentPersistenceListener implements EventListener<AgentAuthEvent> {
    private final Logger logger = LoggerFactory.getLogger(AgentPersistenceListener.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private AgentService clientService;
    @PostConstruct
    public void init() {
        eventBus.register(this);
    }
    @Override
    @Transactional
    public void onEvent(AgentAuthEvent event) {
    }
}
