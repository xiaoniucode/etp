package cn.xilio.vine.console;

import cn.xilio.vine.common.VineBanner;
import cn.xilio.vine.core.Lifecycle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import picocli.CommandLine;


import java.net.URI;

public class ConsoleClient implements Lifecycle {
    private String username;
    private String password;
    private String remoteHost = "127.0.0.1";
    private int remotePort = 9871;
    private static final String SERVER_URI_PATTERN = "ws://%s:%d/command";
    private Channel channel;

    @Override
    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // HTTP编解码器
                            pipeline.addLast(new HttpClientCodec());
                            // 聚合HTTP响应
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            // WebSocket客户端协议处理器
                            pipeline.addLast(new WebSocketClientProtocolHandler(
                                    URI.create(String.format(SERVER_URI_PATTERN, remoteHost, remotePort)),
                                    WebSocketVersion.V13,
                                    null,
                                    false,
                                    new DefaultHttpHeaders(),
                                    5000
                            ));
                            // 自定义响应处理器
                            pipeline.addLast(new ClientResponseHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(remoteHost, remotePort).sync();
            VineBanner.welcome();
            channel = future.channel();
            ChannelHelper.set(channel);
            //处理命令行输入
            new Thread(new InputHandler()).start();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                System.err.println("关闭通道失败: " + e.getMessage());
            }
        }
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
