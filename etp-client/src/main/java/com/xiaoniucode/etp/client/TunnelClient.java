package com.xiaoniucode.etp.client;

import com.xiaoniucode.etp.client.common.utils.MavenArchiverUtil;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.event.ApplicationInitEvent;
import com.xiaoniucode.etp.client.handler.tunnel.RealServerHandler;
import com.xiaoniucode.etp.client.handler.tunnel.ControlTunnelHandler;
import com.xiaoniucode.etp.client.handler.utils.MessageWrapper;
import com.xiaoniucode.etp.client.helper.TunnelClientHelper;
import com.xiaoniucode.etp.client.listener.ApplicationInitListener;
import com.xiaoniucode.etp.client.manager.BootstrapManager;
import com.xiaoniucode.etp.client.manager.EventBusManager;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.handler.IdleCheckHandler;
import com.xiaoniucode.etp.core.tls.SslContextFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.concurrent.atomic.AtomicInteger;

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
            if (!checkConfig(config)) {
                System.exit(0);
                return;
            }
            logger.debug("代理客户端应用初始化");
            EventBusManager.register(new ApplicationInitListener());
            EventBusManager.publishAsync(new ApplicationInitEvent(config));
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
                            ch.pipeline().addLast(new RealServerHandler());
                        }
                    });

            if (config.getTlsConfig().getEnable()) {
                tlsContext = tlsContext = SslContextFactory.createClientSslContext(config.getTlsConfig());
            }
            /**
             * 控制消息处理，单例
             */
            ControlTunnelHandler controlTunnelHandler = new ControlTunnelHandler(ctx -> {
                // 重置重试计数器
                retryCount.set(0);
                //服务器断开 执行重试 重新连接
                if (!stop) {
                    scheduleReconnect();
                }
            });
            controlWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            controlBootstrap.group(controlWorkerGroup)
                    .channel(NettyEventLoopFactory.socketChannelClass())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (config.getTlsConfig().getEnable()) {
                                SslHandler sslHandler = tlsContext.newHandler(sc.alloc());
                                sc.pipeline().addLast("tls", sslHandler);
                            }
                            sc.pipeline()
                                    .addLast("protoBufVarint32FrameDecoder", new ProtobufVarint32FrameDecoder())
                                    .addLast("protoBufDecoder", new ProtobufDecoder(Message.ControlMessage.getDefaultInstance()))
                                    .addLast("protoBufVarint32LengthFieldPrepender", new ProtobufVarint32LengthFieldPrepender())
                                    .addLast("protoBufEncoder", new ProtobufEncoder())
                                    .addLast("idleCheckHandler", new IdleCheckHandler(60, 30, 0, TimeUnit.SECONDS))
                                    .addLast("controlTunnelHandler", controlTunnelHandler);
                        }
                    });

            BootstrapManager.initBootstraps(controlBootstrap, realBootstrap);
            if (!stop) {
                //连接到服务器
                connectTunnelServer();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean checkConfig(AppConfig config) {
        AuthConfig auth = config.getAuthConfig();
        if (!StringUtils.hasText(auth.getToken())) {
            logger.error("请配置登陆密钥");
            return false;
        }
        return true;
    }

    private void connectTunnelServer() {
        ChannelFuture channelFuture = controlBootstrap.connect(config.getServerAddr(), config.getServerPort());
        channelFuture.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                Channel control = channelFuture.channel();
                //获取客户端版本
                String version = MavenArchiverUtil.getVersion();
                // todo test
                String clientId = "EWGSSGREGGWEWE";
                String token = config.getAuthConfig().getToken();
                Message.ControlMessage message = MessageWrapper.buildLogin(clientId, token, version);
                control.writeAndFlush(message).addListener(future -> {
                    if (future.isSuccess()) {
                        control.attr(ChannelConstants.CLIENT_ID).set(clientId);
                    }
                });
                retryCount.set(0);
                logger.debug("连接到ETP服务端: {}:{}", config.getServerAddr(), config.getServerPort());
                start = true;
            } else {
                //连接失败，执行重连
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (retryCount.get() >= config.getAuthConfig().getMaxRetries()) {
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
            return config.getAuthConfig().getInitialDelay();
        }
        // 指数退避 + 随机抖动(±30%)
        long delay = Math.min((1L << retries), config.getAuthConfig().getMaxDelay());
        long jitter = (long) (delay * 0.3 * (Math.random() * 2 - 1));
        return Math.min(delay + jitter, config.getAuthConfig().getMaxDelay());
    }

    @Override
    public void stop() {
        if (controlWorkerGroup != null) {
            stop = true;
            controlWorkerGroup.shutdownGracefully();
        }
        EventBusManager.shutdown();
    }
}
