package com.xiaoniucode.etp.server.listener;

import com.xiaoniucode.etp.common.utils.PortFileUtil;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import com.xiaoniucode.etp.core.event.EventListener;
import com.xiaoniucode.etp.server.config.domain.ClientInfo;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.event.ConfigInitializedEvent;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 监听所有代理端口
 */
public class BindTcpPortListener implements EventListener<ConfigInitializedEvent> {
    private final Logger logger = LoggerFactory.getLogger(BindTcpPortListener.class);

    @Override
    public void onEvent(ConfigInitializedEvent event) {
        TcpProxyServer tcpProxyServer = event.getTcpProxyServer();
        ServerBootstrap serverBootstrap = tcpProxyServer.getServerBootstrap();
        Map<Integer, Channel> portToChannel = tcpProxyServer.getPortToChannel();

        try {
            Collection<ClientInfo> clients = ClientManager.allClients();
            List<StringBuilder> bindPorts = new ArrayList<>();
            for (ClientInfo client : clients) {
                List<ProxyConfig> proxyConfigs = client.getTcpProxies();
                for (ProxyConfig proxy : proxyConfigs) {
                    if (proxy.getStatus() == 1 && ProtocolType.TCP == proxy.getType()) {
                        Integer remotePort = proxy.getRemotePort();
                        if (PortManager.isPortAvailable(remotePort)) {
                            ChannelFuture future = serverBootstrap.bind(remotePort).sync();
                            portToChannel.put(remotePort, future.channel());
                            StringBuilder portItem = new StringBuilder();
                            portItem.append(client.getName()).append("\t")
                                    .append(proxy.getName()).append("\t")
                                    .append(proxy.getType().name()).append("\t")
                                    .append(proxy.getLocalPort()).append("\t")
                                    .append(remotePort);
                            bindPorts.add(portItem);
                            logger.info("成功绑定端口: {}", remotePort);
                        } else {
                            //todo 需要优化
                            logger.warn("未成功启动服务，remotePort:{}端口不可用！", remotePort);
                        }
                    }
                    PortManager.addRemotePort(proxy.getRemotePort());
                }
                if (!bindPorts.isEmpty()) {
                    PortFileUtil.writePortsToFile(bindPorts);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
