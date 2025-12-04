package cn.xilio.etp.server;


import cn.xilio.etp.core.NettyEventLoopFactory;
import cn.xilio.etp.core.Lifecycle;
import cn.xilio.etp.core.IdleCheckHandler;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.server.handler.ControlChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 隧道服务容器
 *
 * @author liuxin
 */
public class TunnelServer implements Lifecycle {
    private static final Logger logger = LoggerFactory.getLogger(TunnelServer.class);
    private final static String DEFAULT_HOST = "0.0.0.0";
    private String host;
    private int port;
    private boolean ssl;
    private EventLoopGroup tunnelBossGroup;
    private EventLoopGroup tunnelWorkerGroup;
    private SslContext tlsContext;
    /**
     * 用于隧道服务启动成功后回调通知调用者
     */
    private Consumer<Void> onSuccessCallback;

    @SuppressWarnings("all")
    @Override
    public void start() {
        try {
            if (ssl) {
                tlsContext = new ServerTlsContextFactory().createContext();
            }
            tunnelBossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                    .childOption(ChannelOption.TCP_NODELAY, true)    // 禁用Nagle算法，降低延迟
                    .childOption(ChannelOption.SO_KEEPALIVE, true)   // 启用TCP心跳检测
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // 使用内存池
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (ssl) {
                                sc.pipeline().addLast("tls", tlsContext.newHandler(sc.alloc()));
                            }
                            sc.pipeline()
                                    .addLast(new ProtobufVarint32FrameDecoder())
                                    .addLast(new ProtobufDecoder(TunnelMessage.Message.getDefaultInstance()))
                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
                                    .addLast(new ProtobufEncoder())
                                    .addLast(new IdleCheckHandler(60, 40, 0, TimeUnit.SECONDS))
                                    .addLast(new ControlChannelHandler());
                        }
                    });
            serverBootstrap.bind(host == null ? DEFAULT_HOST : host, port).sync();
            onSuccessCallback.accept(null);
            logger.info("ETP服务启动成功:{}:{}", host, port);
        } catch (Throwable e) {
            logger.error("服务启动失败",e);
        }
    }

    @Override
    public void stop() {
        try {
            tunnelBossGroup.shutdownGracefully().sync();
            tunnelWorkerGroup.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 隧道启动和认证成功后回调通知调用者处理后续的逻辑
     *
     * @param consumer 消费者
     */
    public void onSuccessListener(Consumer<Void> consumer) {
        this.onSuccessCallback = consumer;
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
