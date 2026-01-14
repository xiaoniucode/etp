package com.xiaoniucode.etp.server.web.core.server;

import com.xiaoniucode.etp.core.NettyEventLoopFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Web容器服务
 *
 * @author liuxin
 */
public class NettyWebServer implements WebServer {
    private final Logger logger = LoggerFactory.getLogger(NettyWebServer.class);
    private String addr;
    private Integer port;
    private List<Filter> filters = new ArrayList<>();
    private Router router = new Router();
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;

    public NettyWebServer() {
        bossEventLoopGroup = NettyEventLoopFactory.eventLoopGroup(1);
        workerEventLoopGroup = NettyEventLoopFactory.eventLoopGroup(1);
        filters.add(new SessionFilter());
    }

    @Override
    public void start() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, 60))
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpContentCompressor())
                                    .addLast(new HttpObjectAggregator(64 * 1024))
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new FlushConsolidationHandler(256, true))
                                    .addLast(new HttpRequestHandler(filters, router));
                        }
                    });
            serverBootstrap.bind(getAddr(), getPort()).sync();
        } catch (Exception e) {
            logger.error("管理界面服务启动失败:{}", e.getMessage());
        }
    }

    @Override
    public void stop() {
        workerEventLoopGroup.shutdownGracefully();
        bossEventLoopGroup.shutdownGracefully();
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    public String getAddr() {
        return addr;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public Router getRouter() {
        return router;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
