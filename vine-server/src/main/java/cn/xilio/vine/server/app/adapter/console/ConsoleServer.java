package cn.xilio.vine.server.app.adapter.console;

import cn.xilio.vine.core.EventLoopUtils;
import cn.xilio.vine.core.Lifecycle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleServer implements Lifecycle {
    private final static Logger logger= LoggerFactory.getLogger(ConsoleServer.class);
    private  EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int port = 9871;
    @Override
    public void start() {
        EventLoopUtils.ServerConfig config = EventLoopUtils.createServerEventLoopConfig();
        bossGroup = config.bossGroup;
        workerGroup = config.workerGroup;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(config.serverChannelClass)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // HTTP编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合HTTP请求
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            // WebSocket协议处理器
                            pipeline.addLast(new WebSocketServerProtocolHandler("/command"));
                            // 自定义命令处理器
                            pipeline.addLast(new CommandHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("启动ConsoleServer成功|端口：{}", port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("ConsoleServer已停止");
    }
}
