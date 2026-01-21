package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.server.config.AppConfig;
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

import java.util.Map;

/**
 * Http proxy server
 *
 * @author xiaoniucode
 */
public class HttpProxyServer implements Lifecycle {
    private final Logger logger = LoggerFactory.getLogger(HttpProxyServer.class);
    private static final HttpProxyServer instance = new HttpProxyServer();
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    //private int httpProxyPort = 8080;
    private HttpProxyServer() {
    }

    public static HttpProxyServer get() {
        return instance;
    }

    @Override
    public void start() {
        try {
            int httpProxyPort= ConfigHelper.get().getHttpProxyPort();
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
            serverBootstrap.bind(httpProxyPort).syncUninterruptibly().get();
            logger.debug("HttpProxyServer started on port {}", httpProxyPort);
        } catch (Exception e) {
            logger.error("HttpProxyServer start error!", e);
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
