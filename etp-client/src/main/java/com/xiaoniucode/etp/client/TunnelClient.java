package com.xiaoniucode.etp.client;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.ConfigHelper;
import com.xiaoniucode.etp.client.handler.RealChannelHandler;
import com.xiaoniucode.etp.client.handler.ControlChannelHandler;
import com.xiaoniucode.etp.client.helper.TunnelClientHelper;
import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.client.security.ClientTlsContextFactory;
import com.xiaoniucode.etp.client.utils.OSUtils;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.msg.Login;
import com.xiaoniucode.etp.core.codec.TunnelMessageCodec;
import com.xiaoniucode.etp.core.IdleCheckHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

/**
 * 代理客户端服务容器
 *
 * @author liuxin
 */
public final class TunnelClient implements Lifecycle {
    private final static Logger logger = LoggerFactory.getLogger(TunnelClient.class);
    private volatile boolean stop = false;
    private volatile boolean start = false;
    private final AppConfig config;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    /**
     * 控制隧道BootStrap
     */
    private Bootstrap controlBootstrap;
    /**
     * 控制隧道工作线程组
     */
    private EventLoopGroup controlWorkerGroup;
    /**
     * SSL加密上下文
     */
    private SslContext tlsContext;
    /**
     * 连接到服务端后通知调用者
     */
    private Consumer<Void> connectSuccessListener;

    public TunnelClient(AppConfig config) {
        this.config = config;
    }

    @SuppressWarnings("all")
    @Override
    public void start() {
        try {
            if (start) {
                return;
            }
            ConfigHelper.set(config);
            TunnelClientHelper.setTunnelClient(this);
            controlBootstrap = new Bootstrap();
            Bootstrap realBootstrap = new Bootstrap();

            realBootstrap.group(NettyEventLoopFactory.eventLoopGroup())
                    .channel(NettyEventLoopFactory.socketChannelClass())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new FlushConsolidationHandler(256, true));
                            ch.pipeline().addLast(new RealChannelHandler());
                        }
                    });

            if (config.isTls()) {
                tlsContext = new ClientTlsContextFactory().createContext();
            }
            controlWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            controlBootstrap.group(controlWorkerGroup)
                    .channel(NettyEventLoopFactory.socketChannelClass())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (config.isTls()) {
                                SSLEngine engine = tlsContext.newEngine(sc.alloc(), config.getServerAddr(), config.getServerPort());
                                engine.setUseClientMode(true);
                                sc.pipeline().addLast("tls", new SslHandler(engine));
                            }
                            sc.pipeline()
                                    .addLast("tunnelMessageCodec",new TunnelMessageCodec())
                                    .addLast(new IdleCheckHandler(60, 30, 0, TimeUnit.SECONDS))
                                    .addLast("controlChannelHandler",new ControlChannelHandler(ctx -> {
                                        // 重置重试计数器
                                        retryCount.set(0);
                                        //服务器断开 执行重试 重新连接
                                        if (!stop) {
                                            scheduleReconnect();
                                        }
                                    }));
                        }
                    });

            ChannelManager.initBootstraps(controlBootstrap, realBootstrap);
            if (!stop) {
                //连接到服务器
                connectTunnelServer();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void connectTunnelServer() {
        ChannelFuture future = controlBootstrap.connect(config.getServerAddr(), config.getServerPort());
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                Channel controlChannel = channelFuture.channel();
                controlChannel.attr(EtpConstants.SERVER_DDR).set(config.getServerAddr());
                controlChannel.attr(EtpConstants.SERVER_PORT).set(config.getServerPort());
                controlChannel.attr(EtpConstants.SECRET_KEY).set(config.getSecretKey());
                ChannelManager.setControlChannel(controlChannel);
                String os = OSUtils.getOS();
                String arch = OSUtils.getOSArch();
                future.channel().writeAndFlush(new Login(config.getSecretKey(), os, arch));
                retryCount.set(0);
                logger.info("已连接到ETP服务端: {}:{}", config.getServerAddr(), config.getServerPort());
                start = true;
                if (connectSuccessListener != null) {
                    connectSuccessListener.accept(null);
                }
            } else {
                //连接失败，执行重连
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (retryCount.get() >= config.getMaxRetries()) {
            logger.error("达到最大重试次数，停止重连");
            this.stop();
            return;
        }
        int retries = retryCount.getAndIncrement();
        long delay = calculateDelay();
        logger.error("连接失败，第{}次重连将在{}秒后执行", retries + 1, delay);
        controlWorkerGroup.schedule(this::connectTunnelServer, delay, TimeUnit.SECONDS);
    }

    /**
     * 指数退避算法，计算重连延迟时间
     *
     * @return 时间（秒）
     */
    private long calculateDelay() {
        int retries = retryCount.get();
        if (retries == 0) {
            return config.getInitialDelaySec();
        }
        // 指数退避 + 随机抖动(±30%)
        long delay = Math.min((1L << retries), config.getMaxDelaySec());
        long jitter = (long) (delay * 0.3 * (Math.random() * 2 - 1));
        return Math.min(delay + jitter, config.getMaxDelaySec());
    }

    @Override
    public void stop() {
        if (controlWorkerGroup != null) {
            stop = true;
            controlWorkerGroup.shutdownGracefully();
        }
    }

    public void onConnectSuccessListener(Consumer<Void> connectCallback) {
        this.connectSuccessListener = connectCallback;
    }

    public AppConfig getConfig() {
        return config;
    }
}
