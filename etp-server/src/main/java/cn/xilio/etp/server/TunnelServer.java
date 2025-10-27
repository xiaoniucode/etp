package cn.xilio.etp.server;


import cn.xilio.etp.core.NettyEventLoopFactory;
import cn.xilio.etp.core.Lifecycle;
import cn.xilio.etp.core.heart.IdleCheckHandler;
import cn.xilio.etp.core.protocol.TunnelMessageDecoder;
import cn.xilio.etp.core.protocol.TunnelMessageEncoder;
import cn.xilio.etp.server.handler.TunnelChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
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
 * @author liuxin
 */
public class TunnelServer implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(TunnelServer.class);
    /**
     * 绑定地址
     */
    private String host = "0.0.0.0";
    /**
     * 绑定端口
     */
    private int port;
    /**
     * 是否开启SSL加密传输
     */
    private boolean ssl;
    private EventLoopGroup tunnelBossGroup;
    private EventLoopGroup tunnelWorkerGroup;
    private SslContext sslContext;

    @Override
    public void start() {
        try {
            if (ssl) {
                sslContext = new ServerSslContextFactory().createContext();
            }
            tunnelBossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (ssl) {
                                sc.pipeline().addLast("ssl", sslContext.newHandler(sc.alloc()));
                            }
                            sc.pipeline()
                                    .addLast(new TunnelMessageDecoder(1024 * 1024, 0, 4, 0, 0))
                                    .addLast(new TunnelMessageEncoder())
                                    .addLast(new IdleCheckHandler(60, 40, 0, TimeUnit.SECONDS))
                                    .addLast(new TunnelChannelHandler());
                        }
                    });
            if (host != null) {
                serverBootstrap.bind(host, port).sync();
            } else {
                serverBootstrap.bind(port).sync();
            }
            //绑定所有代理端口
            TcpProxyServer.getInstance().start();
       /*     // 异步绑定代理所有端口
            CompletableFuture.runAsync(() -> {
                try {
                    TcpProxyServer.getInstance().start();
                    LOGGER.info("端口映射服务已成功启动");
                } catch (Exception e) {
                    LOGGER.error("启动端口映射服务失败: {}", e.getMessage(), e);
                }
            });*/
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            tunnelBossGroup.shutdownGracefully().sync();
            tunnelWorkerGroup.shutdownGracefully().sync();
            //关闭所有绑定的代理端口，释放资源
            TcpProxyServer.getInstance().stop();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
}
