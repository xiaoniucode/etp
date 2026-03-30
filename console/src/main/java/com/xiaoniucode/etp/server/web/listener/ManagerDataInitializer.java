package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.TokenInfo;
import com.xiaoniucode.etp.server.event.TunnelServerStartingEvent;
import com.xiaoniucode.etp.server.security.TokenManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManagerDataInitializer implements EventListener<TunnelServerStartingEvent> {
    private final Logger logger = LoggerFactory.getLogger(ManagerDataInitializer.class);
    @Autowired
    private EventBus eventBus;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TunnelServerStartingEvent event) {

    }
}
