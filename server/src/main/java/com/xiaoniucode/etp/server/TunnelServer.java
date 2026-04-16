package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.codec.TMSPCodec;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.server.Lifecycle;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.transport.tls.TlsHelper;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.TransportConfig;
import com.xiaoniucode.etp.server.configuration.SpringContextHolder;
import com.xiaoniucode.etp.server.event.TunnelServerBindEvent;
import com.xiaoniucode.etp.server.event.TunnelServerStartingEvent;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.transport.ControlFrameHandler;
import com.xiaoniucode.etp.core.transport.TlsContextHolder;
import com.xiaoniucode.etp.server.transport.ControlIdleCheckHandler;
import com.xiaoniucode.etp.server.transport.DownloadRateLimitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.TimeUnit;

/**
 * 控制隧道服务容器
 *
 * @author liuxin
 */
public class TunnelServer implements Lifecycle {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(TunnelServer.class);
    private final AppConfig config;
    private EventLoopGroup tunnelBossGroup;
    private EventLoopGroup tunnelWorkerGroup;
    private SslContext tlsContext;
    private final EventBus eventBus;
    private final ControlFrameHandler controlFrameHandler;

    public TunnelServer(AppConfig config, EventBus eventBus, ControlFrameHandler controlFrameHandler) {
        this.config = config;
        this.eventBus = eventBus;
        this.controlFrameHandler = controlFrameHandler;
    }

    @SuppressWarnings("all")
    @Override
    @PostConstruct
    public void start() {
        try {
            logger.debug("正在启动ETP服务");
            eventBus.publishSync(new TunnelServerStartingEvent());
            TransportConfig transportConfig = config.getTransportConfig();
            TlsConfig tlsConfig = transportConfig.getTlsConfig();

            if (tlsConfig == null || (tlsConfig != null && tlsConfig.isEnabled())) {
                tlsContext = TlsHelper.buildSslContext(false, tlsConfig, tlsConfig == null);
                TlsContextHolder.initialize(tlsContext);
            }
            tunnelBossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();

            LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
            DownloadRateLimitHandler downloadRateLimitHandler = SpringContextHolder.getBean(DownloadRateLimitHandler.class);
            AgentManager agentManager = SpringContextHolder.getBean(AgentManager.class);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (tlsContext != null) {
                                sc.pipeline().addLast(new OptionalSslHandler(tlsContext));
                            }
                            sc.pipeline()
                                    .addLast(loggingHandler)
                                    .addLast(NettyConstants.TMSP_CODEC, TMSPCodec.create(10 * 1024 * 1024))
                                    .addLast(downloadRateLimitHandler)
                                    .addLast(NettyConstants.CONTROL_IDLE_CHECK_HANDLER, new ControlIdleCheckHandler(agentManager,90,0,0, TimeUnit.SECONDS))
                                    .addLast(NettyConstants.CONTROL_FRAME_HANDLER, controlFrameHandler);
                        }
                    });
            serverBootstrap.bind(config.getServerAddr(), config.getServerPort()).sync();
            logger.info("ETP隧道已开启:{}:{}", config.getServerAddr(), config.getServerPort());
            eventBus.publishAsync(new TunnelServerBindEvent());
        } catch (Throwable e) {
            logger.error("ETP隧道开启失败", e);
        }
    }

    @Override
    @PreDestroy
    public void stop() {
        try {
            tunnelBossGroup.shutdownGracefully().sync();
            tunnelWorkerGroup.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}
