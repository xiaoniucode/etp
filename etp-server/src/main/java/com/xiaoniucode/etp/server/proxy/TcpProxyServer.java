package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.common.PortFileUtil;
import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.server.handler.visitor.TcpVisitorHandler;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.server.manager.PortAllocator;
import com.xiaoniucode.etp.server.manager.RuntimeStateManager;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import com.xiaoniucode.etp.server.config.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    /**
     * 公网端口和启动的服务channel映射，用于通过端口快速找到对应的channel
     */
    private final Map<Integer, Channel> remotePortChannelMapping = new ConcurrentHashMap<>();
    private final PortAllocator portAllocator;
    private final RuntimeStateManager state = RuntimeStateManager.get();

    private TcpProxyServer() {
        this.portAllocator = PortAllocator.get();
    }

    @Override
    public void start() {
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
                        sc.pipeline().addLast(new TcpVisitorHandler());
                    }
                });
        bindAllProxyPort();
        LOGGER.debug("所有端口映射服务启动完成");
    }

    /**
     * 如果端口映射的status=1则启动
     */
    private void bindAllProxyPort() {
        try {
            Collection<ClientInfo> clients = state.allClients();
            List<StringBuilder> bindPorts = new ArrayList<>();
            for (ClientInfo client : clients) {
                List<ProxyMapping> proxyMappings = client.getTcpProxies();
                for (ProxyMapping proxy : proxyMappings) {
                    if (proxy.getStatus() == 1) {
                        Integer remotePort = proxy.getRemotePort();
                        if (portAllocator.isPortAvailable(remotePort)) {
                            ChannelFuture future = serverBootstrap.bind(remotePort).sync();
                            remotePortChannelMapping.put(remotePort, future.channel());
                            StringBuilder portItem = new StringBuilder();
                            portItem.append(client.getName()).append("\t")
                                    .append(proxy.getName()).append("\t")
                                    .append(proxy.getType().name()).append("\t")
                                    .append(proxy.getLocalPort()).append("\t")
                                    .append(remotePort);
                            bindPorts.add(portItem);
                            LOGGER.info("成功绑定端口: {}", remotePort);
                        } else {
                            LOGGER.warn("未成功启动服务，remotePort:{}端口不可用！", remotePort);
                        }
                    }
                    PortAllocator.get().addRemotePort(proxy.getRemotePort());
                }
                if (!bindPorts.isEmpty()) {
                    PortFileUtil.writePortsToFile(bindPorts);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 启动一个指定的公网端口服务
     *
     * @param remotePort 需要启动的公网端口
     */
    public void startRemotePort(Integer remotePort) {
        try {
            if (!remotePortChannelMapping.containsKey(remotePort)) {
                ChannelFuture future = serverBootstrap.bind(remotePort).sync();
                remotePortChannelMapping.put(remotePort, future.channel());
                portAllocator.addRemotePort(remotePort);
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
        return remotePortChannelMapping.size();
    }

    /**
     * 停掉一个指定的公网端口服务,停止继续监听，后面无法再连接
     *
     * @param remotePort  需要停止的公网端口
     * @param releasePort 是否释放remotePort端口
     */
    public void stopRemotePort(Integer remotePort, boolean releasePort) {
        try {
            // 1. 先关闭所有已建立的连接
            Set<Channel> connections = ChannelManager.getActiveChannelsByRemotePort(remotePort);
            if (connections != null) {
                for (Channel ch : connections) {
                    ch.close();
                }
                LOGGER.info("已关闭 {} 个活跃连接", connections.size());
                ChannelManager.removeActiveChannels(remotePort);
            }
            // 2. 再关闭监听通道
            Channel serverChannel = remotePortChannelMapping.get(remotePort);
            if (serverChannel != null) {
                serverChannel.close().sync();
                remotePortChannelMapping.remove(remotePort);
            }
            // 3. 释放端口
            if (releasePort) {
                portAllocator.releasePort(remotePort);
                LOGGER.info("成功停止并释放公网端口: {}", remotePort);
                //4.如果释放了端口，说明映射被删掉了，需要清空流量指标收集器
                MetricsCollector.removeCollector(remotePort);
                LOGGER.debug("删除公网端口: {} 流量指标收集器", remotePort);
            } else {
                LOGGER.info("{} 端口映射服务已停止（保留端口）", remotePort);
            }
        } catch (Exception e) {
            LOGGER.error("停止端口 {} 失败", remotePort, e);
        }
    }

    /**
     * 停止TCP代理服务器，关闭所有绑定通道并释放资源
     */
    @Override
    public void stop() {
        try {
            LOGGER.info("开始停止TCP代理服务器");
            // 关闭所有绑定的通道
            for (Channel channel : remotePortChannelMapping.values()) {
                try {
                    channel.close().sync();
                    int port = channel.localAddress() != null ? ((InetSocketAddress) channel.localAddress()).getPort() : -1;
                    if (port != -1) {
                        portAllocator.releasePort(port);
                        LOGGER.info("成功释放端口: {}", port);
                    }
                } catch (Exception e) {
                    LOGGER.error("关闭通道失败: {}", e.getMessage());
                }
            }
            remotePortChannelMapping.clear();
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
}
