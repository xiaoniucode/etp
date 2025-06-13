package cn.xilio.vine.client;

import cn.xilio.vine.client.handler.internal.RealChannelHandler;
import cn.xilio.vine.client.handler.tunnel.TunnelChannelHandler;
import cn.xilio.vine.core.EventLoopUtils;
import cn.xilio.vine.core.Tunnel;
import cn.xilio.vine.core.protocol.TunnelMessage;
import cn.xilio.vine.core.heart.IdleCheckHandler;
import cn.xilio.vine.core.protocol.TunnelMessageDecoder;
import cn.xilio.vine.core.protocol.TunnelMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.TimeUnit;

public class TunnelClient implements Tunnel {
    private boolean ssl;
    private String serverAddr;
    private int serverPort;
    private String secretKey;

    public static void main(String[] args) {
        TunnelClient tunnelClient = new TunnelClient();
        tunnelClient.setServerAddr("localhost");
        tunnelClient.setServerPort(8523);
        tunnelClient.setSecretKey("10086key");
        tunnelClient.start();
    }

    @Override
    public void start() {
        Bootstrap bootstrap = new Bootstrap();
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

        bootstrap.group(eventLoopConfig.workerGroup)
                .channel(eventLoopConfig.clientChannelClass)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline()
                                .addLast(new TunnelMessageDecoder(1024 * 1024, 0, 0, 0, 0))
                                .addLast(new TunnelMessageEncoder())
                                .addLast(new IdleCheckHandler(60, 40, 0, TimeUnit.SECONDS))
                                .addLast(new TunnelChannelHandler(realBootstrap, bootstrap));
                    }
                });
        ChannelFuture future = bootstrap.connect(serverAddr, serverPort);
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                TunnelMessage.Message message = TunnelMessage.Message.newBuilder()
                        .setType(TunnelMessage.Message.Type.AUTH)
                        .setUri(secretKey)
                        .build();
                future.channel().writeAndFlush(message);
                System.out.println("success");
            } else {
                //重新连接
            }
        });
    }

    @Override
    public void stop() {
    }

    private static void checkArgs(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("请指定配置文件！");
        }
        if (!"-c".equalsIgnoreCase(args[0])) {
            throw new IllegalArgumentException("参数有误，请通过-c <path>指定配置文件！");
        }
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
