package com.xiaoniucode.etp.server.port;

import com.xiaoniucode.etp.server.configuration.SpringContextHolder;
import com.xiaoniucode.etp.server.transport.tcp.TcpProxyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PortAcceptor {
    private final Logger logger = LoggerFactory.getLogger(PortAcceptor.class);
    private final Map<Integer, Channel> portToChannel = new ConcurrentHashMap<>();

    public void bindPort(Integer port) {
        try {
            TcpProxyServer tcpProxyServer = SpringContextHolder.getBean(TcpProxyServer.class);
            ServerBootstrap serverBootstrap = tcpProxyServer.getServerBootstrap();
            if (!portToChannel.containsKey(port)) {
                ChannelFuture future = serverBootstrap.bind(port).sync();
                portToChannel.put(port, future.channel());
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void stopPortListen(Integer remotePort) {
        try {
            Channel serverChannel = portToChannel.get(remotePort);
            if (serverChannel != null) {
                serverChannel.close().sync();
                portToChannel.remove(remotePort);
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
                    logger.info("成功释放端口: {}", port);
                }
            } catch (Exception e) {
                logger.error("关闭 channel 失败", e);
            }
        }
        portToChannel.clear();
    }
}
