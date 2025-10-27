package cn.xilio.etp.client;

import cn.xilio.etp.client.handler.internal.RealChannelHandler;
import cn.xilio.etp.client.handler.tunnel.TunnelChannelHandler;
import cn.xilio.etp.common.ansi.AnsiLog;
import cn.xilio.etp.core.NettyEventLoopFactory;
import cn.xilio.etp.core.Lifecycle;
import cn.xilio.etp.core.protocol.TunnelMessage;
import cn.xilio.etp.core.heart.IdleCheckHandler;
import cn.xilio.etp.core.protocol.TunnelMessageDecoder;
import cn.xilio.etp.core.protocol.TunnelMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TunnelClient implements Lifecycle {
    private String serverAddr;
    private int serverPort;
    private String secretKey;
    private boolean ssl;
    /**
     * 初始化重连延迟时间 单位：秒
     */
    private long initialDelaySec = 2;
    /**
     * 最大重试次数 超过以后关闭workerGroup
     */
    private int maxRetries = 5;
    /**
     * 最大延迟时间 如果超过了则取maxDelaySec为最大延迟时间 单位：秒
     */
    private long maxDelaySec = 5;
    /**
     * 用于记录当前重试次数
     */
    private AtomicInteger retryCount = new AtomicInteger(0);
    /**
     * 隧道BootStrap
     */
    private Bootstrap tunnelBootstrap;
    /**
     * 隧道工作线程组
     */
    private EventLoopGroup tunnelWorkerGroup;

    private SslContext sslContext;

    @Override
    public void start() {
        try {
            tunnelBootstrap = new Bootstrap();
            Bootstrap realBootstrap = new Bootstrap();

            realBootstrap.group(NettyEventLoopFactory.eventLoopGroup())
                    .channel(NettyEventLoopFactory.socketChannelClass())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new RealChannelHandler());
                        }
                    });

            if (ssl) {
                sslContext = new ClientSslContextFactory().createContext();
            }
            tunnelWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
            tunnelBootstrap.group(tunnelWorkerGroup)
                    .channel(NettyEventLoopFactory.socketChannelClass())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) {
                            if (ssl) {
                                SSLEngine engine = sslContext.newEngine(sc.alloc(), serverAddr, serverPort);
                                engine.setUseClientMode(true);
                                sc.pipeline().addLast("ssl", new SslHandler(engine));
                            }
                            sc.pipeline()
                                    .addLast(new TunnelMessageDecoder(1024 * 1024, 0, 4, 0, 0))
                                    .addLast(new TunnelMessageEncoder())
                                    .addLast(new IdleCheckHandler(60, 40, 0, TimeUnit.SECONDS))
                                    .addLast(new TunnelChannelHandler(realBootstrap, tunnelBootstrap, ctx -> {
                                        retryCount.set(0); // 重置重试计数器
                                        //服务器断开 执行重试 重新连接
                                        scheduleReconnect();
                                    }));
                        }
                    });
            //连接到服务器
            connectTunnelServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectTunnelServer() {
        ChannelFuture future = tunnelBootstrap.connect(serverAddr, serverPort);
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                //缓存控制隧道
                ChannelManager.setControlTunnelChannel(channelFuture.channel());
                TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                        .setType(TunnelMessage.Message.Type.AUTH)
                        .setExt(secretKey)
                        .build();
                future.channel().writeAndFlush(message);
                retryCount.set(0); // 重置重试计数器
                AnsiLog.info("etp客户端启动成功");
            } else {
                //重新连接
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (retryCount.get() >= maxRetries) {
            System.err.println("达到最大重试次数，停止重连");
            tunnelWorkerGroup.shutdownGracefully();
            return;
        }
        // 计算退避时间 (2^n秒，最大不超过maxDelaySec)
        int retries = retryCount.getAndIncrement();
        //指数计算，如果超过了最大延迟时间，则取最大延迟时间
        // long delay = Math.min((1L << retries), maxDelaySec);
        long delay = calculateDelay();//指数退避 + 随机抖动(±30%)
        System.out.printf("[连接失败] 第%d次重连将在%d秒后执行%n", retries + 1, delay);
        // 调度重连任务
        tunnelWorkerGroup.schedule(() -> {
            System.out.println("重连中...");
            connectTunnelServer();
        }, delay, TimeUnit.SECONDS);
    }

    /**
     * 计算延迟时间
     *
     * @return 时间（秒）
     */
    private long calculateDelay() {
        int retries = retryCount.get();
        if (retries == 0) {
            return initialDelaySec;
        }
        // 指数退避 + 随机抖动(±30%)
        long delay = Math.min((1L << retries), maxDelaySec);
        long jitter = (long) (delay * 0.3 * (Math.random() * 2 - 1));
        return Math.min(delay + jitter, maxDelaySec);
    }

    @Override
    public void stop() {
        tunnelWorkerGroup.shutdownGracefully();
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

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
}
