package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.ProxyUpdatedEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 代理配置更新事件处理
 */
@Component
public class ProxyUpdatedListener implements EventListener<ProxyUpdatedEvent> {
    private final Logger logger = LoggerFactory.getLogger(ProxyUpdatedListener.class);
    @Autowired
    private EventBus eventBus;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onEvent(ProxyUpdatedEvent event) {

    }
}
