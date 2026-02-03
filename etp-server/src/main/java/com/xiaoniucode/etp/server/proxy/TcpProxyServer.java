package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.event.TcpProxyInitializedEvent;
import com.xiaoniucode.etp.server.handler.tunnel.TcpVisitorHandler;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP服务启动、停止、管理
 *
 * @author liuxin
 */
public final class TcpProxyServer implements Lifecycle {
    private static final Logger logger = LoggerFactory.getLogger(TcpProxyServer.class);
    @Getter
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final AtomicBoolean init = new AtomicBoolean(false);
    private final TcpVisitorHandler tcpVisitorHandler;
    private final EventBus eventBus;

    public TcpProxyServer(TcpVisitorHandler tcpVisitorHandler, EventBus eventBus) {
        this.tcpVisitorHandler = tcpVisitorHandler;
        this.eventBus = eventBus;
    }

    @Override
    @PostConstruct
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
                        sc.pipeline().addLast(tcpVisitorHandler);
                    }
                });
        init.set(true);
        eventBus.publishAsync(new TcpProxyInitializedEvent(serverBootstrap));
        logger.debug("TCP 代理服务初始化成功");
    }


    @Override
    @PreDestroy
    public void stop() {
        if (!init.get()) {
            logger.warn("尚未初始化TCP 服务");
            return;
        }
        logger.debug("清理 TCP 代理线程资源");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
