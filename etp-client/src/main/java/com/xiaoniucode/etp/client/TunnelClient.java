package com.xiaoniucode.etp.client;

import com.xiaoniucode.etp.client.handler.RealChannelHandler;
import com.xiaoniucode.etp.client.handler.ControlChannelHandler;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.Lifecycle;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import com.xiaoniucode.etp.core.IdleCheckHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端服务容器
 *
 * @author liuxin
 */
public class TunnelClient implements Lifecycle {
    private final static Logger logger = LoggerFactory.getLogger(TunnelClient.class);
    private String serverAddr;
    private int serverPort;
    private String secretKey;
    private boolean tls;
    /**
     * 初始化重连延迟时间 单位：秒
     */
    private int initialDelaySec = 2;
    /**
     * 最大重试次数 超过以后关闭workerGroup
     */
    private int maxRetries = 5;
    /**
     * 最大延迟时间 如果超过了则取maxDelaySec为最大延迟时间 单位：秒
     */
    private int maxDelaySec = 8;
    /**
     * 用于记录当前重试次数
     */
    private final AtomicInteger retryCount = new AtomicInteger(0);
    /**
     * 控制隧道BootStrap
     */
    private Bootstrap controlBootstrap;
    /**
     * 控制隧道工作线程组
     */
    private EventLoopGroup tunnelWorkerGroup;
    /**
     * SSL加密上下文
     */
    private SslContext tlsContext;
    /**
     * 连接到服务端后通知调用者
     */
    private Consumer<Void> connectSuccessListener;

    public TunnelClient() {
    }

    public TunnelClient(String serverAddr, int serverPort, String secretKey, boolean tls) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.secretKey = secretKey;
        this.tls = tls;
    }

    @SuppressWarnings("all")
    @Override
    public void start() {
        try {
            controlBootstrap = new Bootstrap();
            Bootstrap realBootstrap = new Bootstrap();

            realBootstrap.group(NettyEventLoopFactory.eventLoopGroup())
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new RealChannelHandler());
                    }
                });

            if (tls) {
                tlsContext = new ClientTlsContextFactory().createContext();
            }
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            controlBootstrap.group(tunnelWorkerGroup)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法
                .option(ChannelOption.SO_KEEPALIVE, true) // TCP保活
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // 内存池
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        if (tls) {
                            SSLEngine engine = tlsContext.newEngine(sc.alloc(), serverAddr, serverPort);
                            engine.setUseClientMode(true);
                            sc.pipeline().addLast("tls", new SslHandler(engine));
                        }
                        sc.pipeline()
                            .addLast(new ProtobufVarint32FrameDecoder())
                            .addLast(new ProtobufDecoder(TunnelMessage.Message.getDefaultInstance()))
                            .addLast(new ProtobufVarint32LengthFieldPrepender())
                            .addLast(new ProtobufEncoder())
                            .addLast(new IdleCheckHandler(60, 30, 0, TimeUnit.SECONDS))
                            .addLast(new ControlChannelHandler(ctx -> {
                                // 重置重试计数器
                                retryCount.set(0);
                                //服务器断开 执行重试 重新连接
                                scheduleReconnect();
                            }));
                    }
                });
            ChannelManager.initBootstraps(controlBootstrap, realBootstrap);
            //连接到服务器
            connectTunnelServer();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void connectTunnelServer() {
        ChannelFuture future = controlBootstrap.connect(serverAddr, serverPort);
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                //缓存控制隧道
                Channel channel = channelFuture.channel();
                channel.attr(EtpConstants.SERVER_DDR).set(serverAddr);
                channel.attr(EtpConstants.SECRET_KEY).set(secretKey);
                ChannelManager.setControlChannel(channel);
                TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                    .setType(TunnelMessage.Message.Type.AUTH)
                    .setExt(secretKey)
                    .build();
                future.channel().writeAndFlush(message);
                retryCount.set(0);
                logger.info("已连接到ETP服务端: {}:{}", serverAddr, serverPort);
                connectSuccessListener.accept(null);
            } else {
                //重新连接
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (retryCount.get() >= getMaxRetries()) {
            logger.error("达到最大重试次数，停止重连");
            this.stop();
            return;
        }
        int retries = retryCount.getAndIncrement();
        long delay = calculateDelay();
        logger.error("连接失败，第{}次重连将在{}秒后执行", retries + 1, delay);
        tunnelWorkerGroup.schedule(this::connectTunnelServer, delay, TimeUnit.SECONDS);
    }

    /**
     * 指数退避算法，计算重连延迟时间
     *
     * @return 时间（秒）
     */
    private long calculateDelay() {
        int retries = retryCount.get();
        if (retries == 0) {
            return getInitialDelaySec();
        }
        // 指数退避 + 随机抖动(±30%)
        long delay = Math.min((1L << retries), getMaxDelaySec());
        long jitter = (long) (delay * 0.3 * (Math.random() * 2 - 1));
        return Math.min(delay + jitter, getMaxDelaySec());
    }

    @Override
    public void stop() {
        if (tunnelWorkerGroup != null) {
            tunnelWorkerGroup.shutdownGracefully();
        }
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public int getInitialDelaySec() {
        return initialDelaySec;
    }

    public void setInitialDelaySec(int initialDelaySec) {
        if (initialDelaySec > 0) {
            this.initialDelaySec = initialDelaySec;
        }
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        if (maxRetries > 0) {
            this.maxRetries = maxRetries;
        }
    }

    public int getMaxDelaySec() {
        return maxDelaySec;
    }

    public void setMaxDelaySec(int maxDelaySec) {
        if (maxDelaySec > 0) {
            this.maxDelaySec = maxDelaySec;
        }
    }

    public void onConnectSuccessListener(Consumer<Void> connectCallback) {
        this.connectSuccessListener = connectCallback;
    }
}
