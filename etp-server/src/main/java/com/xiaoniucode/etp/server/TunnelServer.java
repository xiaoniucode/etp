package com.xiaoniucode.etp.server;

import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.IdleCheckHandler;

import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.server.handler.ControlChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
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
    private String host;
    private int port;
    private boolean tls;
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
            logger.debug("正在启动ETP服务");
            if (tls) {
                tlsContext = new ServerTlsContextFactory().createContext();
            }
            tunnelBossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        if (tls) {
                            sc.pipeline().addLast("tls", tlsContext.newHandler(sc.alloc()));
                            logger.debug("TLS加密处理器添加成功");
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
            serverBootstrap.bind(host, port).sync();
            onSuccessCallback.accept(null);
            logger.info("ETP服务启动成功:{}:{}", host, port);
        } catch (Throwable e) {
            logger.error("ETP服务启动失败", e);
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

    public void setTls(boolean tls) {
        this.tls = tls;
    }
}
