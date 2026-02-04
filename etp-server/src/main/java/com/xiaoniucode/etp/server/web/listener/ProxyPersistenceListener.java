package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
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

import java.util.ArrayList;
import java.util.List;
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
    public void onEvent(ProxyCreatedEvent event) {
        String clientId = event.getClientId();
        ProxyConfig proxyConfig = event.getProxyConfig();
        Proxy proxy = toProxy(clientId, proxyConfig);
        proxyRepository.saveAndFlush(proxy);
//        queue.offer(proxy);
//        logger.debug("代理配置已加入队列，客户端: {}, 队列大小: {}", clientId, queue.size());
//
//        // 达到批量大小，触发保存
//        if (queue.size() >= BATCH_SIZE) {
//            flushQueue();
//        }
    }

    private Proxy toProxy(String clientId, ProxyConfig config) {
        Proxy proxy = new Proxy();
        proxy.setClientId(clientId);
        proxy.setName(config.getName());
        proxy.setProtocol(config.getProtocol());
        proxy.setLocalIp(config.getLocalIp());
        proxy.setLocalPort(config.getLocalPort());
        proxy.setStatus(config.getStatus());
        proxy.setRemotePort(config.getRemotePort());
        proxy.setCompress(config.getCompress());
        proxy.setEncrypt(config.getEncrypt());
        proxy.setSource(1);//todo
        return proxy;
    }

    private void flushQueue() {
        synchronized (flushLock) {
            int remaining = queue.size();
            if (remaining > 0) {
                logger.info("队列中剩余待保存数据量: {}", remaining);

                // 取出队列中所有剩余数据
                List<Proxy> remainingData = new ArrayList<>(remaining);

                while (!queue.isEmpty()) {
                    remainingData.add(queue.poll());
                }

            }
        }

        logger.info("代理配置数据保存完成");
    }


    /**
     * 应用关闭之前，将缓存中的全部保存到数据库
     */
    @PreDestroy
    public void destroy() {

    }
}
