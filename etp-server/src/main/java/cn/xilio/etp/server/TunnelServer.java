package cn.xilio.etp.server;


import cn.xilio.etp.core.NettyEventLoopFactory;
import cn.xilio.etp.core.Lifecycle;
import cn.xilio.etp.core.IdleCheckHandler;
import cn.xilio.etp.core.protocol.TunnelMessageDecoder;
import cn.xilio.etp.core.protocol.TunnelMessageEncoder;
import cn.xilio.etp.server.handler.ControlChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author liuxin
 */
public class TunnelServer implements Lifecycle {
    private static final Logger logger = LoggerFactory.getLogger(TunnelServer.class);
    private String host = "0.0.0.0";
    private int port;
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
                                    .addLast(new TunnelMessageDecoder())
                                    .addLast(new TunnelMessageEncoder())
                                    .addLast(new IdleCheckHandler(60, 40, 0, TimeUnit.SECONDS))
                                    .addLast(new ControlChannelHandler());
                        }
                    });
            serverBootstrap.bind(host, port).sync();
            //绑定所有代理端口
            TcpProxyServer.getInstance().start();
            logger.info("代理服务启动成功:{}:{}", host, port);
        } catch (Exception e) {
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if (host != null) {
            this.host = host;
        }
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
