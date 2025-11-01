package cn.xilio.etp.server.web.framework;

import cn.xilio.etp.core.NettyEventLoopFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * @author liuxin
 */
public class NettyWebServer implements WebServer {
    private final Logger logger = LoggerFactory.getLogger(NettyWebServer.class);
    private String addr = "0.0.0.0";
    private Integer port = 5300;
    private List<Filter> filters = new ArrayList<>();
    private Router router = new Router();
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;

    public NettyWebServer() {
        bossEventLoopGroup = NettyEventLoopFactory.eventLoopGroup(1);
        workerEventLoopGroup = NettyEventLoopFactory.eventLoopGroup(1);
    }

    @Override
    public void start() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
                    .channel(NettyEventLoopFactory.serverSocketChannelClass())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(64 * 1024))
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpRequestHandler(filters, router));
                        }
                    });
            serverBootstrap.bind(addr, port).sync();
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

    public void setRouter(Router router) {
        this.router = router;
    }
}
