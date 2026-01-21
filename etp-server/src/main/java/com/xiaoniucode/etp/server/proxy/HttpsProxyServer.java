package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.handler.HostSnifferHandler;
import com.xiaoniucode.etp.server.handler.visitor.HttpVisitorHandler;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Https proxy server
 *
 * @author xiaoniucode
 */
public class HttpsProxyServer implements Lifecycle {
    private final Logger logger = LoggerFactory.getLogger(HttpsProxyServer.class);
    private static final HttpsProxyServer instance = new HttpsProxyServer();
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private HttpsProxyServer() {
    }

    public static HttpsProxyServer get() {
        return instance;
    }

    @Override
    public void start() {
        try {
            int httpsProxyPort= ConfigHelper.get().getHttpsProxyPort();
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
                            sc.pipeline().addLast(new HostSnifferHandler());
                            sc.pipeline().addLast(new TrafficMetricsHandler());
                            sc.pipeline().addLast(new FlushConsolidationHandler(256, true));
                            sc.pipeline().addLast(new HttpVisitorHandler());
                        }
                    });
            serverBootstrap.bind(httpsProxyPort).syncUninterruptibly().get();
            logger.debug("Https server started on port {}", httpsProxyPort);
        } catch (Exception e) {
            logger.error("Https proxy server start error!", e);
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
