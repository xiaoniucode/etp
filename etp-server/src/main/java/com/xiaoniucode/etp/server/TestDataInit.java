package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.event.TcpServerInitializedEvent;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class TestDataInit implements EventListener<TcpServerInitializedEvent> {
    private Logger logger = LoggerFactory.getLogger(TestDataInit.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ProxyManager proxyManager;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TcpServerInitializedEvent event) {

        ProxyConfig proxyConfig1 = new ProxyConfig();
        proxyConfig1.setProtocol(ProtocolType.TCP);
        proxyConfig1.setRemotePort(3307);
        proxyConfig1.setLocalIp("localhost");
        proxyConfig1.setLocalPort(3306);

        ProxyConfig proxyConfig2 = new ProxyConfig();
        proxyConfig2.setProtocol(ProtocolType.HTTP);
        proxyConfig2.setLocalIp("localhost");
        proxyConfig2.setLocalPort(8081);
        proxyConfig2.setFullDomains(new HashSet<>(List.of("a.domain1.com")));

        proxyManager.addProxy("1", proxyConfig1, null);
        proxyManager.addProxy("1", proxyConfig2, null);
        logger.info("初始化测试数据");
    }
}
