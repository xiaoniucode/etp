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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class ConsoleClient implements Lifecycle {
    private static final String SERVER_URI = "ws://localhost:9871/command";

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
                                    URI.create(SERVER_URI),
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
            ChannelFuture future = bootstrap.connect(new URI(SERVER_URI).getHost(), new URI(SERVER_URI).getPort()).sync();
            VineBanner.welcome();
            // 用户输入命令
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\u001B[32mvine > \u001B[0m");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) break;

                // 发送命令到服务端
                if (future.channel().isActive()) {
                    future.channel().writeAndFlush(new TextWebSocketFrame(input));
                } else {
                    System.err.println("连接未就绪");
                }
            }
            scanner.close();
            future.channel().closeFuture().sync();
        } catch (InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }


}
