package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.handler.IdleCheckHandler;

import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.server.helper.BeanHelper;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.ConfigUtils;
import com.xiaoniucode.etp.server.event.TunnelBindEvent;
import com.xiaoniucode.etp.server.handler.tunnel.ControlTunnelHandler;
import com.xiaoniucode.etp.server.manager.DomainManager;
import com.xiaoniucode.etp.server.manager.PortManager;
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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 控制隧道服务容器
 *
 * @author liuxin
 */
public class TunnelServer implements Lifecycle {
    private static final Logger logger = LoggerFactory.getLogger(TunnelServer.class);
    private final AppConfig config;
    private EventLoopGroup tunnelBossGroup;
    private EventLoopGroup tunnelWorkerGroup;
    private SslContext tlsContext;

    public TunnelServer(AppConfig config) {
        this.config = config;
    }

    @SuppressWarnings("all")
    @Override
    @PostConstruct
    public void start() {
        try {
            logger.debug("正在启动ETP服务");
//            if (config.isTls()) {
//                tlsContext = new ServerTlsContextFactory().createContext();
//            }
            tunnelBossGroup = NettyEventLoopFactory.eventLoopGroup(1);
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(tunnelBossGroup, tunnelWorkerGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
//                            if (config.isTls()) {
//                                sc.pipeline().addLast("tls", tlsContext.newHandler(sc.alloc()));
//                                logger.debug("TLS加密处理器添加成功");
//                            }
                            sc.pipeline()
                                    .addLast("protoBufVarint32FrameDecoder", new ProtobufVarint32FrameDecoder())
                                    .addLast("protoBufDecoder", new ProtobufDecoder(Message.ControlMessage.getDefaultInstance()))
                                    .addLast("protoBufVarint32LengthFieldPrepender", new ProtobufVarint32LengthFieldPrepender())
                                    .addLast("protoBufEncoder", new ProtobufEncoder())
                                    .addLast("idleCheckHandler",new IdleCheckHandler(60, 40, 0, TimeUnit.SECONDS))
                                    .addLast("controlTunnelHandler",BeanHelper.getBean(ControlTunnelHandler.class));
                        }
                    });
            serverBootstrap.bind(config.getServerAddr(), config.getServerPort()).sync();
            //异步处理
            CompletableFuture.runAsync(() -> {
                BeanHelper.getBean(TcpProxyServer.class).start();
                BeanHelper.getBean(HttpProxyServer.class).start();
            });
            logger.info("ETP隧道已开启:{}:{}", config.getServerAddr(), config.getServerPort());
            BeanHelper.getBean(EventBus.class).publishAsync(new TunnelBindEvent());
        } catch (Throwable e) {
            logger.error("ETP隧道开启失败", e);
        }
    }
    @Override
    @PreDestroy
    public void stop() {
        try {
            tunnelBossGroup.shutdownGracefully().sync();
            tunnelWorkerGroup.shutdownGracefully().sync();
            BeanHelper.getBean(TcpProxyServer.class).stop();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}
