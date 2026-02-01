package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.notify.EventListener;
import com.xiaoniucode.etp.server.event.TcpServerInitializedEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 系统首次启动时候执行，当TCP代理服务初始化成功后，将所有代理端口（remotePort）进行绑定
 */
@Component
public class PortBinder implements EventListener<TcpServerInitializedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PortBinder.class);
    @Autowired
    private EventBus eventBus;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onEvent(TcpServerInitializedEvent event) {

//        try {
            //todo test


//
//            ServerBootstrap serverBootstrap = event.getServerBootstrap();
//            Map<Integer, Channel> portToChannel = event.getPortToChannel();
//            Collection<ClientInfo> clients = ClientManager.allClients();
//            for (ClientInfo client : clients) {
//                List<ProxyConfig> proxyConfigs = client.getTcpProxies();
//                for (ProxyConfig proxy : proxyConfigs) {
//                    if (proxy.getStatus() == ProxyStatus.CLOSED) {
//                        continue;
//                    }
//                    Integer remotePort = proxy.getRemotePort();
//                    if (PortManager.isPortAvailable(remotePort)) {
//                        ChannelFuture future = serverBootstrap.bind(remotePort).sync();
//                        portToChannel.put(remotePort, future.channel());
//                        logger.info("成功绑定端口: {}", remotePort);
//                    } else {
//                        logger.warn("端口不可用，跳过绑定: {}", remotePort);
//                    }
//                    PortManager.addPort(proxy.getRemotePort());
//                }
//            }
//        } catch (Exception e) {
//            logger.error("绑定端口失败: {}", e.getMessage(), e);
//        }
    }
}
