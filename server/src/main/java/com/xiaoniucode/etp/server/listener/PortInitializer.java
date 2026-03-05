package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.TcpProxyInitializedEvent;
import com.xiaoniucode.etp.server.event.TunnelServerBindEvent;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.manager.PortListenerManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 系统首次启动时候执行，当TCP代理服务初始化成功后，将所有代理端口（remotePort）进行监听
 */
@Component
public class PortInitializer implements EventListener<TunnelServerBindEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PortInitializer.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private PortManager portManager;
    @Autowired
    private PortListenerManager portListenerManager;

    @Autowired
    private ProxyManager proxyManager;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TunnelServerBindEvent event) {
        try {
            ProxyConfig proxyConfig = new ProxyConfig();
            Collection<ProxyConfig> configs = proxyManager.getTcpProxies();
            portListenerManager.bindPort(8033);//todo
            portListenerManager.bindPort(3307);//todo
            portListenerManager.bindPort(8608);//todo
            for (ProxyConfig proxy : configs) {
                if (proxy.getStatus() == ProxyStatus.CLOSED) {
                    continue;
                }
                Integer remotePort = proxy.getRemotePort();
                if (portManager.isAvailable(remotePort)) {
                    portListenerManager.bindPort(remotePort);

                    logger.info("成功绑定端口: {}", remotePort);
                } else {
                    logger.warn("端口不可用，跳过绑定: {}", remotePort);
                }
            }
        } catch (Exception e) {
            logger.error("绑定端口失败: {}", e.getMessage(), e);
        }
    }
}
