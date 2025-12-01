package cn.xilio.etp.server;

import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.common.PortFileUtil;
import cn.xilio.etp.core.Lifecycle;
import cn.xilio.etp.core.NettyEventLoopFactory;
import cn.xilio.etp.server.handler.ClientChannelHandler;
import cn.xilio.etp.server.manager.PortAllocator;
import cn.xilio.etp.server.manager.RuntimeState;
import cn.xilio.etp.server.metrics.TrafficMetricsHandler;
import cn.xilio.etp.server.config.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 负责启动和管理TCP代理服务。
 *
 * @author liuxin
 */
public final class TcpProxyServer implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProxyServer.class);
    private static volatile TcpProxyServer instance = new TcpProxyServer();

    public static TcpProxyServer get() {
        return instance;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    /**
     * 公网端口和启动的服务channel映射，用于通过端口快速找到对应的channel
     */
    private final Map<Integer, Channel> remotePortChannelMapping = new ConcurrentHashMap<>();
    private final PortAllocator portAllocator;
    private ServerBootstrap serverBootstrap;
    private final RuntimeState state = RuntimeState.get();

    private TcpProxyServer() {
        this.portAllocator = PortAllocator.getInstance();
    }

    @Override
    public void start() {
        LOGGER.info("开始启动代理服务");
        bossGroup = NettyEventLoopFactory.eventLoopGroup(1);
        workerGroup = NettyEventLoopFactory.eventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        sc.pipeline().addLast(new TrafficMetricsHandler());
                        sc.pipeline().addLast(new ClientChannelHandler());/*公网访问者处理器*/
                    }
                });
        bindAllProxyPort();
    }

    private void bindAllProxyPort() {
        try {
            Collection<ClientInfo> clients = state.allClients();
            List<StringBuilder> bindPorts = new ArrayList<>();
            for (ClientInfo client : clients) {
                List<ProxyMapping> proxyMappings = client.getProxies();
                for (ProxyMapping proxy : proxyMappings) {
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
                        LOGGER.warn("remotePort:{}端口正在被占用！", remotePort);
                    }
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
     * 停掉一个指定的公网端口服务
     *
     * @param remotePort  需要停止的公网端口
     * @param releasePort 是否释放remotePort端口
     */
    public void stopRemotePort(Integer remotePort, boolean releasePort) {
        try {
            Channel channel = remotePortChannelMapping.get(remotePort);
            if (channel != null) {
                try {
                    // 关闭通道
                    channel.close().sync();
                    // 是否释放端口资源
                    if (releasePort) {
                        portAllocator.releasePort(remotePort);
                        LOGGER.info("成功停止并释放公网端口: {}", remotePort);
                    }
                    LOGGER.info("{} 端口映射服务已停止", remotePort);
                } catch (InterruptedException e) {
                    LOGGER.error("停止端口 {} 失败: {}", remotePort, e.getMessage(), e);
                    Thread.currentThread().interrupt();
                } finally {
                    remotePortChannelMapping.remove(remotePort);
                }
            } else {
                LOGGER.warn("未找到绑定在端口 {} 的服务", remotePort);
            }
        } finally {
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
            // 优雅关闭事件循环组
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            LOGGER.info("TCP代理服务器已停止");
        } catch (Exception e) {
            LOGGER.error("TCP代理服务器停止失败", e);
        } finally {
        }
    }
}
