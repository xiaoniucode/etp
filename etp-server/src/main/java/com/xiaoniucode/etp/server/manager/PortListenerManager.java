package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PortListenerManager {
    private final Logger logger = LoggerFactory.getLogger(PortListenerManager.class);
    private final Map<Integer, Channel> portToChannel = new ConcurrentHashMap<>();
    @Autowired
    private PortManager portManager;
    @Autowired
    private VisitorSessionManager visitorSessionManager;
    @Autowired
    private TcpProxyServer tcpProxyServer;

    /**
     * 监听指定端口，用于访问者连接
     *
     * @param port 需要监听的公网端口
     */
    public void bindPort(Integer port) {
        try {
            ServerBootstrap serverBootstrap = tcpProxyServer.getServerBootstrap();
            if (!portToChannel.containsKey(port)) {
                ChannelFuture future = serverBootstrap.bind(port).sync();
                portToChannel.put(port, future.channel());
                portManager.addPort(port);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取正在监听的服务数量
     */
    public int getOnlinePort() {
        return portToChannel.size();
    }

    public void stopPortListen(Integer remotePort, boolean releasePort) {
        try {
            //关闭所有已建立的访问者连接
            visitorSessionManager.closeVisitorsByRemotePort(remotePort);
            Channel serverChannel = portToChannel.get(remotePort);
            if (serverChannel != null) {
                serverChannel.close().sync();
                portToChannel.remove(remotePort);
            }
            if (releasePort) {
                portManager.release(remotePort);
            }
        } catch (Exception e) {
            logger.error("停止服务失败：端口-{}", remotePort, e);
        }
    }

    @PreDestroy
    public void clearAll() {
        logger.debug("清理代理端口资源占用");
        for (Channel channel : portToChannel.values()) {
            try {
                channel.close().sync();
                int port = channel.localAddress() != null ? ((InetSocketAddress) channel.localAddress()).getPort() : -1;
                if (port != -1) {
                    portManager.release(port);
                    logger.info("成功释放端口: {}", port);
                }
            } catch (Exception e) {
                logger.error("关闭 channel 失败", e);
            }
        }
        portToChannel.clear();
    }
}
