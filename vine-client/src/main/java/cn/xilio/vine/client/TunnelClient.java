package cn.xilio.vine.client;

import cn.xilio.vine.client.handler.internal.RealChannelHandler;
import cn.xilio.vine.client.handler.tunnel.TunnelChannelHandler;
import cn.xilio.vine.core.EventLoopUtils;
import cn.xilio.vine.core.ServerLife;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.core.heart.IdleCheckHandler;
import cn.xilio.vine.core.protocol.TunnelMessageDecoder;
import cn.xilio.vine.core.protocol.TunnelMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TunnelClient implements ServerLife {
    private boolean ssl;
    private String serverAddr;
    private int serverPort;
    private String secretKey;
    /**
     * 初始化重连延迟时间 单位：秒
     */
    private long initialDelaySec = 2;
    /**
     * 最大重试次数 超过以后关闭workerGroup
     */
    private int maxRetries = 10;
    /**
     * 最大延迟时间 如果超过了则取maxDelaySec为最大延迟时间 单位：秒
     */
    private long maxDelaySec = 10;
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

    @Override
    public void start() {
        tunnelBootstrap = new Bootstrap();
        Bootstrap realBootstrap = new Bootstrap();
        EventLoopUtils.ClientConfig eventLoopConfig = EventLoopUtils.createClientEventLoopConfig();
        realBootstrap.group(eventLoopConfig.workerGroup)
                .channel(eventLoopConfig.clientChannelClass)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RealChannelHandler());
                    }
                });
        tunnelWorkerGroup = eventLoopConfig.workerGroup;
        tunnelBootstrap.group(tunnelWorkerGroup)
                .channel(eventLoopConfig.clientChannelClass)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
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
    }

    private void connectTunnelServer() {
        ChannelFuture future = tunnelBootstrap.connect(serverAddr, serverPort);
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                        .setType(TunnelMessage.Message.Type.AUTH)
                        .setExt(secretKey)
                        .build();
                future.channel().writeAndFlush(message);
                retryCount.set(0); // 重置重试计数器
                System.out.println("成功连接到代理服务器～");
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
        System.out.printf("第%d次重连将在%d秒后执行...%n", retries + 1, delay);
        // 调度重连任务
        tunnelWorkerGroup.schedule(() -> {
            System.out.println("执行重连...");
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

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
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


}
