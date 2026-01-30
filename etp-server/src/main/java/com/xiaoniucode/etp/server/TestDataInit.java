package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.event.TcpServerInitializedEvent;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.manager.TcpServerManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TestDataInit implements EventListener<TcpServerInitializedEvent> {
    private Logger logger = LoggerFactory.getLogger(TestDataInit.class);
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private DomainManager domainManager;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TcpServerInitializedEvent event) {

        ProxyConfig proxyConfig1 = new ProxyConfig();
        proxyConfig1.setName("mysql");
        proxyConfig1.setProtocol(ProtocolType.TCP);
        proxyConfig1.setRemotePort(3307);
        proxyConfig1.setLocalIp("localhost");
        proxyConfig1.setLocalPort(3306);

        ProxyConfig redicConfig = new ProxyConfig();
        redicConfig.setName("redis");
        redicConfig.setProtocol(ProtocolType.TCP);
        redicConfig.setRemotePort(3308);
        redicConfig.setLocalIp("localhost");
        redicConfig.setLocalPort(6379);

        ProxyConfig proxyConfig2 = new ProxyConfig();
        proxyConfig2.setName("web");
        proxyConfig2.setProtocol(ProtocolType.HTTP);
        proxyConfig2.setLocalIp("localhost");
        proxyConfig2.setLocalPort(8081);
        proxyConfig2.getCustomDomains().add("a.domain1.com");
        proxyConfig2.getCustomDomains().add("a2.domain1.com");

        Set<String> domains = domainManager.addDomainsSmartly(proxyConfig2);
        proxyConfig2.getFullDomains().addAll(domains);


        proxyManager.createProxy("1", proxyConfig1, null);
        proxyManager.createProxy("1", proxyConfig2, null);
        proxyManager.createProxy("1", redicConfig, null);

        BeanHelper.getBean(TcpServerManager.class).bindPort(3307);
        BeanHelper.getBean(TcpServerManager.class).bindPort(3308);
        logger.info("初始化测试数据");
    }
}
