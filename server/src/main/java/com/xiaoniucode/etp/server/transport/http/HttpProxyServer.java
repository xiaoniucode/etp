package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http proxy server
 *
 * @author xiaoniucode
 */
public class HttpProxyServer implements Lifecycle {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final HttpVisitorHandler httpVisitorHandler;
    private final AppConfig appConfig;
    private final EventBus eventBus;
    private final TrafficMetricsHandler trafficMetricsHandler;
    private final HttpIpCheckHandler httpIpCheckHandler;
    private final BasicAuthHandler basicAuthHandler;

    public HttpProxyServer(AppConfig config, HttpVisitorHandler httpVisitorHandler, HttpIpCheckHandler httpIpCheckHandler, BasicAuthHandler basicAuthHandler, EventBus eventBus, TrafficMetricsHandler trafficMetricsHandler) {
        this.appConfig = config;
        this.httpVisitorHandler = httpVisitorHandler;
        this.eventBus = eventBus;
        this.trafficMetricsHandler = trafficMetricsHandler;
        this.httpIpCheckHandler = httpIpCheckHandler;
        this.basicAuthHandler = basicAuthHandler;
    }

    @Override
    @PostConstruct
    public void start() {
        try {
            int httpProxyPort = appConfig.getHttpProxyPort();
            bossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            workerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
//                    .childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(64 * 1024, 256 * 1024))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            sc.pipeline().addLast(new HostSnifferHandler());
                             sc.pipeline().addLast(httpIpCheckHandler);
                            //  sc.pipeline().addLast(basicAuthHandler);
                            //  sc.pipeline().addLast(trafficMetricsHandler);
                            //   sc.pipeline().addLast(new FlushConsolidationHandler(256, true));
                            sc.pipeline().addLast(NettyConstants.HTTP_VISITOR_HANDLER, httpVisitorHandler);
                        }
                    });
            serverBootstrap.bind(httpProxyPort).syncUninterruptibly().get();
            logger.debug("http proxy server started on port {}", httpProxyPort);
        } catch (Exception e) {
            logger.error("http proxy server start error!", e);
        }
    }

    @Override
    @PreDestroy
    public void stop() {
        logger.debug("清理 HTTP 代理线程资源");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
