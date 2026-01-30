package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.event.TcpServerInitializedEvent;
import com.xiaoniucode.etp.server.handler.tunnel.ResourceReleaseHandler;
import com.xiaoniucode.etp.server.handler.tunnel.TcpVisitorHandler;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.manager.TcpServerManager;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProxyServer.class);
    @Getter
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final AtomicBoolean init = new AtomicBoolean(false);

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
                        sc.pipeline().addLast(BeanHelper.getBean(TcpVisitorHandler.class));
                    }
                });
        init.set(true);
        BeanHelper.getBean(EventBus.class).publishAsync(new TcpServerInitializedEvent(serverBootstrap));
        LOGGER.debug("TCP 代理服务初始化成功");
    }


    @Override
    public void stop() {
        if (!init.get()) {
            LOGGER.warn("尚未初始化TCP 服务");
            return;
        }
        BeanHelper.getBean(TcpServerManager.class).clearAll();
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
