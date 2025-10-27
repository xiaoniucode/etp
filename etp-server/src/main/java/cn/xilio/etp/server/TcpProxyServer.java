package cn.xilio.etp.server;

import cn.xilio.etp.core.Lifecycle;
import cn.xilio.etp.core.NettyEventLoopFactory;
import cn.xilio.etp.server.handler.VisitorChannelHandler;
import cn.xilio.etp.server.store.ClientInfo;
import cn.xilio.etp.server.store.Config;
import cn.xilio.etp.server.store.ProxyMapping;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * TCP代理服务器类（单例模式），负责启动和管理TCP代理服务。
 *
 * @author liuxin
 */
public class TcpProxyServer implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProxyServer.class);
    // 单例实例，volatile确保多线程可见性
    private static volatile TcpProxyServer instance;
    // 可重入锁，确保端口绑定和资源管理的线程安全
    private final ReentrantLock lock = new ReentrantLock();
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    // 保存绑定的通道，用于优雅关闭
    private final List<Channel> boundChannels = new ArrayList<>();
    // 动态端口分配器
    private final PortAllocator portAllocator;
    // 默认端口范围
    private static final int DEFAULT_MIN_PORT = 8000;
    private static final int DEFAULT_MAX_PORT = 9000;

    /**
     * 私有构造函数，初始化端口分配器
     */
    private TcpProxyServer() {
        this.portAllocator = PortAllocator.getInstance();
        LOGGER.info("TcpProxyServer实例初始化");
    }

    /**
     * 获取单例实例（双重检查锁定）
     *
     * @return TcpProxyServer 单例实例
     */
    public static TcpProxyServer getInstance() {
        if (instance == null) {
            synchronized (TcpProxyServer.class) {
                if (instance == null) {
                    instance = new TcpProxyServer();
                }
            }
        }
        return instance;
    }

    /**
     * 启动TCP代理服务器，绑定到Config中的端口或动态分配的端口
     */
    @Override
    public void start() {
        lock.lock();
        try {
            LOGGER.info("开始启动TCP代理服务器");
            bossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            workerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            sc.pipeline().addLast(new VisitorChannelHandler());
                        }
                    });
            try {
                //绑定所有客户端端口代理
                List<ClientInfo> clients = Config.getInstance().getClients();
                List<StringBuilder> bindPorts = new ArrayList<>();
                if (clients != null && !clients.isEmpty()) {
                    for (ClientInfo client : clients) {
                        List<ProxyMapping> proxyMappings = client.getProxyMappings();
                        if (proxyMappings != null && !proxyMappings.isEmpty()) {
                            for (ProxyMapping proxy : proxyMappings) {
                                Integer remotePort = proxy.getRemotePort();
                                //如果用户没有指定远程端口，则由系统随机生成
                                if (remotePort == null) {
                                    //随机分配一个可用的端口⚠️
                                    int allocatePort = portAllocator.allocateAvailablePort();
                                    ChannelFuture future = serverBootstrap.bind(allocatePort).sync();
                                    StringBuilder portItem = new StringBuilder();
                                    portItem.append(client.getName()).append("\t")
                                            .append(proxy.getName()).append("\t")
                                            .append(proxy.getType().name()).append("\t")
                                            .append(proxy.getLocalPort()).append("\t")
                                            .append(allocatePort);
                                    bindPorts.add(portItem);
                                    boundChannels.add(future.channel());
                                    //将新分配的端口记录到分配器缓存
                                    portAllocator.addPort(allocatePort);
                                    //将远程端口和内网端口映射信息记录到全局配置
                                    proxy.setRemotePort(allocatePort);
                                    Config.getInstance().addClientPublicNetworkPortMapping(client.getSecretKey(), allocatePort);
                                    Config.getInstance().getPortLocalServerMapping().put(allocatePort, proxy.getLocalPort());
                                    LOGGER.info("成功绑定端口: {}", allocatePort);
                                } else {
                                    //检查用户指定的端口是否可用，如果不可用抛出异常信息，不影响其他代理端口的启动
                                    if (portAllocator.isPortAvailable(remotePort)) {
                                        ChannelFuture future = serverBootstrap.bind(remotePort).sync();
                                        boundChannels.add(future.channel());
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

                            }
                        }
                    }
                    //打印已绑定的服务
                    if (!bindPorts.isEmpty()){
                        System.out.println("------------------------已绑定的端口------------------------");
                        for (StringBuilder item : bindPorts) {
                            System.out.println(item.toString());
                        }
                        System.out.println("----------------------------------------------------------");
                    }

                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 停止TCP代理服务器，关闭所有绑定通道并释放资源
     */
    @Override
    public void stop() {
        lock.lock();
        try {
            LOGGER.info("开始停止TCP代理服务器");
            // 关闭所有绑定的通道
            for (Channel channel : boundChannels) {
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
            boundChannels.clear();
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
            lock.unlock();
        }
    }
}
