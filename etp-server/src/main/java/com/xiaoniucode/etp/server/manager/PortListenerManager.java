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
                logger.info("{} 服务启动成功", port);
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
            //关闭所有已建立的连接
            visitorSessionManager.closeVisitorsByRemotePort(remotePort);
            //关闭监听通道
            Channel serverChannel = portToChannel.get(remotePort);
            if (serverChannel != null) {
                serverChannel.close().sync();
                portToChannel.remove(remotePort);
            }
            //释放端口
            if (releasePort) {
                portManager.release(remotePort);
                logger.info("成功停止并释放公网端口: {}", remotePort);
                //如果释放了端口，说明映射被删掉了，需要清空流量指标收集器
                MetricsCollector.removeCollector(remotePort + "");
                logger.debug("删除公网端口: {}", remotePort);
            } else {
                logger.info("{} 端口映射服务已停止（保留端口）", remotePort);
            }
        } catch (Exception e) {
            logger.error("停止端口 {} 失败", remotePort, e);
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
