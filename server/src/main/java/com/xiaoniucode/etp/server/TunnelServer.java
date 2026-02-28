package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.codec.TMSPCodec;
import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.netty.IdleCheckHandler;

import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.tls.SslContextFactory;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.event.TunnelServerBindEvent;
import com.xiaoniucode.etp.server.event.TunnelServerStartingEvent;
import com.xiaoniucode.etp.server.transport.ControlFrameHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            if (config.getTlsConfig().getEnable()) {
                tlsContext = SslContextFactory.createServerSslContext(config.getTlsConfig());
            }
            tunnelBossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(256 * 1024, 4 * 1024 * 1024))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (config.getTlsConfig().getEnable()) {
                                sc.pipeline().addLast("tls", tlsContext.newHandler(sc.alloc()));
                                logger.debug("TLS加密处理器添加成功");
                            }
                            sc.pipeline()
                                    .addLast(new TMSPCodec.Decoder(10 * 1024 * 1024))
                                    .addLast(new TMSPCodec.Encoder())
                                    .addLast("idleCheckHandler", new IdleCheckHandler(60, 60, 0, TimeUnit.SECONDS))
                                    .addLast("controlFrameHandler", controlFrameHandler);
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
