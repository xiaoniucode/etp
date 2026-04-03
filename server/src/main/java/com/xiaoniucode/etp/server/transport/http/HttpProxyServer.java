/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.transport.http;

import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.transport.NettyEventLoopFactory;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Http proxy server
 *
 * @author xiaoniucode
 */
public class HttpProxyServer implements Lifecycle {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(HttpProxyServer.class);
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
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            sc.pipeline().addLast(new HostSnifferHandler());
                            sc.pipeline().addLast(httpIpCheckHandler);
                            sc.pipeline().addLast(basicAuthHandler);
                            //  sc.pipeline().addLast(trafficMetricsHandler);
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
