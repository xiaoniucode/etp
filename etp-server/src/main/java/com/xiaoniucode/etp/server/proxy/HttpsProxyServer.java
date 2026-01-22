package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.server.config.ConfigHelper;
import com.xiaoniucode.etp.server.handler.HostSnifferHandler;
import com.xiaoniucode.etp.server.handler.visitor.HttpVisitorHandler;
import com.xiaoniucode.etp.server.metrics.TrafficMetricsHandler;
import com.xiaoniucode.etp.server.security.ServerTlsContextFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.Mapping;
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
            SslContext sslContext = new ServerTlsContextFactory().createContext();
            int httpsProxyPort= ConfigHelper.get().getHttpsProxyPort();
            bossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            workerGroup = NettyEventLoopFactory.eventLoopGroup();
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 64 * 1024) // 接收缓冲区大小
                    .childOption(ChannelOption.SO_SNDBUF, 64 * 1024) // 发送缓冲区大小
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            sc.pipeline().addLast(new SniHandler(new Mapping<String, SslContext>() {
                                @Override
                                public SslContext map(String input) {
                                    return sslContext;
                                }
                            }));
                            sc.pipeline().addLast(new TrafficMetricsHandler());
                            sc.pipeline().addLast(new HostSnifferHandler());
                            sc.pipeline().addLast(new FlushConsolidationHandler(256, true));
                            sc.pipeline().addLast(new HttpVisitorHandler());
                        }
                    });
            serverBootstrap.bind(httpsProxyPort).syncUninterruptibly().get();
            logger.info("HTTPS proxy server started on port {}", httpsProxyPort);
        } catch (Exception e) {
            logger.error("HTTPS proxy server start error!", e);
        }
    }

    @Override
    public void stop() {
        logger.info("Stopping HTTPS proxy server...");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("HTTPS proxy server stopped");
    }
}
