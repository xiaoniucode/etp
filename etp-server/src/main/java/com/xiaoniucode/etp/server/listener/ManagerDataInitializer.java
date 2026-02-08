package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.AccessTokenInfo;
import com.xiaoniucode.etp.server.event.TunnelServerStartingEvent;
import com.xiaoniucode.etp.server.manager.AccessTokenManager;
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
    @Resource
    private AppConfig appConfig;
    @Autowired
    private AccessTokenManager accessTokenManager;
    @Autowired
    private EventBus eventBus;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TunnelServerStartingEvent event) {
        List<AccessTokenInfo> accessTokens = appConfig.getAccessTokens();
        accessTokenManager.addAccessTokens(accessTokens);
        logger.debug("添加 {}个 访问令牌到管理器",accessTokens.size());
    }
}
