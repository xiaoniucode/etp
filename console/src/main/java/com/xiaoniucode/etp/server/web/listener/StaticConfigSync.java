package com.xiaoniucode.etp.server.web.listener;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.domain.*;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.event.TunnelServerBindEvent;
import com.xiaoniucode.etp.server.web.repository.AccessTokenRepository;
import com.xiaoniucode.etp.server.web.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
/**
 * 数据同步，如果开启了管理界面，则将静态配置同步到数据库
 */
@Component
public class StaticConfigSync implements EventListener<TunnelServerBindEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StaticConfigSync.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Resource
    private AppConfig config;
    @PostConstruct
    public void init() {
        eventBus.register(this);
    }
    @Override
    public void onEvent(TunnelServerBindEvent event) {
        try {
            if (config.getDashboard().getEnabled()) {
            } else {
                logger.debug("管理面板未启用，跳过数据同步");
            }
        } catch (Exception e) {
            logger.error("数据同步失败", e);
            throw e; 
        }
    }
}
