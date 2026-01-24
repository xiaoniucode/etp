package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.server.handler.visitor.TcpVisitorHandler;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP服务启动、停止、管理
 *
 * @author liuxin
 */
public final class TcpProxyServer implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProxyServer.class);
    private static final TcpProxyServer instance = new TcpProxyServer();

    public static TcpProxyServer get() {
        return instance;
    }

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final AtomicBoolean init = new AtomicBoolean(false);

    private final Map<Integer, Channel> portToChannel = new ConcurrentHashMap<>();

    private TcpProxyServer() {
    }

    @Override
    public void start() {
        if (init.get()) {
            return;
        }
        bossGroup = NettyEventLoopFactory.eventLoopGroup(1);
        workerGroup = NettyEventLoopFactory.eventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        sc.pipeline().addLast(new TrafficMetricsHandler());
                        sc.pipeline().addLast(new FlushConsolidationHandler(256, true));
                        sc.pipeline().addLast("tcpVisitorHandler", new TcpVisitorHandler());
                    }
                });
        init.set(true);
        LOGGER.debug("TCP代理服务初始化成功");
    }



    /**
     * 启动一个指定的公网端口服务
     *
     * @param remotePort 需要启动的公网端口
     */
    public void startRemotePort(Integer remotePort) {
        try {
            if (!portToChannel.containsKey(remotePort)) {
                ChannelFuture future = serverBootstrap.bind(remotePort).sync();
                portToChannel.put(remotePort, future.channel());
                PortManager.addRemotePort(remotePort);
                LOGGER.info("{} 服务启动成功", remotePort);
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 获取正在运行的服务数量
     */
    public int runningPortCount() {
        return portToChannel.size();
    }

    public void stopRemotePort(Integer remotePort, boolean releasePort) {
        try {
            // 1. 先关闭所有已建立的连接
            ChannelManager.closeVisitor(remotePort);
            // 2. 再关闭监听通道
            Channel serverChannel = portToChannel.get(remotePort);
            if (serverChannel != null) {
                serverChannel.close().sync();
                portToChannel.remove(remotePort);
            }
            // 3. 释放端口
            if (releasePort) {
                PortManager.release(remotePort);
                LOGGER.info("成功停止并释放公网端口: {}", remotePort);
                //4.如果释放了端口，说明映射被删掉了，需要清空流量指标收集器
                MetricsCollector.removeCollector(remotePort + "");
                LOGGER.debug("删除公网端口: {} 流量指标收集器", remotePort);
            } else {
                LOGGER.info("{} 端口映射服务已停止（保留端口）", remotePort);
            }
        } catch (Exception e) {
            LOGGER.error("停止端口 {} 失败", remotePort, e);
        }
    }


    @Override
    public void stop() {
        try {
            if (!init.get()) {
                LOGGER.warn("尚未初始化TCP服务");
                return;
            }
            LOGGER.info("开始停止TCP代理服务器");
            // 关闭所有绑定的通道
            for (Channel channel : portToChannel.values()) {
                try {
                    channel.close().sync();
                    int port = channel.localAddress() != null ? ((InetSocketAddress) channel.localAddress()).getPort() : -1;
                    if (port != -1) {
                        PortManager.release(port);
                        LOGGER.info("成功释放端口: {}", port);
                    }
                } catch (Exception e) {
                    LOGGER.error("关闭通道失败: {}", e.getMessage());
                }
            }
            portToChannel.clear();
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            LOGGER.info("TCP代理服务器已停止");
        } catch (Exception e) {
            LOGGER.error("TCP代理服务器停止失败", e);
        }
    }
    public ServerBootstrap getServerBootstrap() {
        return serverBootstrap;
    }

    public Map<Integer, Channel> getPortToChannel() {
        return portToChannel;
    }
}
