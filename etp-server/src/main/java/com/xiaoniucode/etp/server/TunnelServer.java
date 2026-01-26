package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.IdleCheckHandler;

import com.xiaoniucode.etp.core.codec.TunnelMessageCodec;
import com.xiaoniucode.etp.core.event.GlobalEventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.event.DatabaseInitEvent;
import com.xiaoniucode.etp.server.event.ConfigInitializedEvent;
import com.xiaoniucode.etp.server.event.TunnelBindEvent;
import com.xiaoniucode.etp.server.handler.ControlTunnelHandler;
import com.xiaoniucode.etp.server.listener.ConfigRegisterListener;
import com.xiaoniucode.etp.server.listener.DatabaseInitListener;
import com.xiaoniucode.etp.server.listener.StaticConfigInitListener;
import com.xiaoniucode.etp.server.listener.BindTcpPortListener;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.PortManager;
import com.xiaoniucode.etp.server.proxy.HttpProxyServer;
import com.xiaoniucode.etp.server.proxy.HttpsProxyServer;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.security.ServerTlsContextFactory;
import com.xiaoniucode.etp.server.web.DashboardApi;
import com.xiaoniucode.etp.server.web.core.server.NettyWebServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 控制隧道服务容器
 *
 * @author liuxin
 */
public class TunnelServer implements Lifecycle {
    private static final Logger logger = LoggerFactory.getLogger(TunnelServer.class);
    private final AppConfig config;
    private EventLoopGroup tunnelBossGroup;
    private EventLoopGroup tunnelWorkerGroup;
    private SslContext tlsContext;
    private NettyWebServer webServer;

    public TunnelServer(AppConfig config) {
        this.config = config;
    }

    @SuppressWarnings("all")
    @Override
    public void start() {
        try {
            GlobalEventBus.get().subscribe(TunnelBindEvent.class, new DatabaseInitListener());
            GlobalEventBus.get().subscribe(TunnelBindEvent.class, new StaticConfigInitListener());
            GlobalEventBus.get().subscribe(DatabaseInitEvent.class, new ConfigRegisterListener());
            GlobalEventBus.get().subscribe(ConfigInitializedEvent.class, new BindTcpPortListener());
            //todo 需要优化，避免影响启动速度
            ConfigHelper.set(config);
            PortManager.init(config);
            DomainManager.init(config);
            logger.debug("正在启动ETP服务");
            if (config.isTls()) {
                tlsContext = new ServerTlsContextFactory().createContext();
            }
            tunnelBossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (config.isTls()) {
                                sc.pipeline().addLast("tls", tlsContext.newHandler(sc.alloc()));
                                logger.debug("TLS加密处理器添加成功");
                            }
                            sc.pipeline()
                                    .addLast("tunnelMessageCodec", new TunnelMessageCodec())
                                    .addLast(new IdleCheckHandler(60, 40, 0, TimeUnit.SECONDS))
                                    .addLast("controlTunnelHandler", new ControlTunnelHandler());
                        }
                    });
            serverBootstrap.bind(config.getHost(), config.getBindPort()).sync();
            //异步处理
            CompletableFuture.runAsync(() -> {
                //1.初始化管理面板
                initDashboard();
                //2.开启TCP代理
                TcpProxyServer.get().start();
                //3.开启HTTP代理
                HttpProxyServer.get().start();
                //4.开启HTTPS代理
                HttpsProxyServer.get().start();
            });
            logger.info("ETP隧道已开启:{}:{}", config.getHost(), config.getBindPort());
            GlobalEventBus.get().publishAsync(new TunnelBindEvent());
        } catch (Throwable e) {
            logger.error("ETP隧道开启失败", e);
        }
    }

    private void initDashboard() {
        if (config.getDashboard().getEnable()) {
            webServer = new NettyWebServer();
            webServer.setAddr(config.getDashboard().getAddr());
            webServer.setPort(config.getDashboard().getPort());
            DashboardApi.initFilters(webServer.getFilters());
            DashboardApi.initRoutes(webServer.getRouter());
            webServer.start();
            logger.info("Dashboard图形面板启动成功，浏览器访问：{}:{}", webServer.getAddr(), webServer.getPort());
        }
    }

    @Override
    public void stop() {
        try {
            tunnelBossGroup.shutdownGracefully().sync();
            tunnelWorkerGroup.shutdownGracefully().sync();
            TcpProxyServer.get().stop();
            if (webServer != null) {
                webServer.stop();
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    public AppConfig getConfig() {
        return config;
    }
}
