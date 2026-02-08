package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.ProxyCreatedEvent;
import com.xiaoniucode.etp.server.web.domain.Proxy;
import com.xiaoniucode.etp.server.web.repository.ProxyDomainRepository;
import com.xiaoniucode.etp.server.web.repository.ProxyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
@Component
public class ProxyPersistenceListener implements EventListener<ProxyCreatedEvent> {
    private final Logger logger = LoggerFactory.getLogger(ProxyPersistenceListener.class);
    /**
     * 缓存累计到一定量再一次新保存到数据库
     */
    private static final int BATCH_SIZE = 100;
    private final Queue<Proxy> queue = new ConcurrentLinkedQueue<>();
    private final Object flushLock = new Object();
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private ProxyDomainRepository proxyDomainRepository;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    @Transactional
    public void onEvent(ProxyCreatedEvent event) {
        String clientId = event.getClientId();
        ClientType clientType = event.getClientType();
        ProxyConfig config = event.getProxyConfig();
        Proxy proxy = toProxy(clientId, clientType, config);
        ProtocolType protocol = config.getProtocol();
        if (ProtocolType.isTcp(protocol)) {
            proxyRepository.saveAndFlush(proxy);
        } else if (ProtocolType.isHttp(protocol)) {
            proxyRepository.saveAndFlush(proxy);

        }
    }

    private Proxy toProxy(String clientId, ClientType clientType, ProxyConfig config) {
        Proxy proxy = new Proxy();
        proxy.setClientId(clientId);
        proxy.setName(config.getName());
        proxy.setProtocol(config.getProtocol());
        proxy.setLocalIp(config.getLocalIp());
        proxy.setLocalPort(config.getLocalPort());
        proxy.setStatus(config.getStatus());
        proxy.setCompress(config.getCompress());
        proxy.setEncrypt(config.getEncrypt());
        proxy.setClientType(clientType);

        if (ProtocolType.isTcp(config.getProtocol())) {
            proxy.setRemotePort(config.getRemotePort());
        }
        if (ProtocolType.isHttp(config.getProtocol())) {
            proxy.setDomainType(config.getDomainType());
        }
        return proxy;
    }


    /**
     * 应用关闭之前，将缓存中的全部保存到数据库
     */
    @PreDestroy
    public void destroy() {

    }
}
