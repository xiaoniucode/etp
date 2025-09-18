package cn.xilio.etp.core;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.*;
import io.netty.channel.kqueue.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.*;

/**
 * EventLoop工具类
 */
public final class EventLoopUtils {
    private EventLoopUtils() {
    }

    /**
     * 创建服务端EventLoop配置
     */
    public static ServerConfig createServerEventLoopConfig() {
        if (Epoll.isAvailable()) {
            return new ServerConfig(
                    new EpollEventLoopGroup(1),
                    new EpollEventLoopGroup(),
                    EpollServerSocketChannel.class
            );
        } else if (KQueue.isAvailable()) {
            return new ServerConfig(
                    new KQueueEventLoopGroup(1),
                    new KQueueEventLoopGroup(),
                    KQueueServerSocketChannel.class
            );
        } else {
            return new ServerConfig(
                    new NioEventLoopGroup(1),
                    new NioEventLoopGroup(),
                    NioServerSocketChannel.class
            );
        }
    }

    /**
     * 创建客户端EventLoop配置
     */
    public static ClientConfig createClientEventLoopConfig() {
        if (Epoll.isAvailable()) {
            return new ClientConfig(
                    new EpollEventLoopGroup(),
                    EpollSocketChannel.class
            );
        } else if (KQueue.isAvailable()) {
            return new ClientConfig(
                    new KQueueEventLoopGroup(),
                    KQueueSocketChannel.class
            );
        } else {
            return new ClientConfig(
                    new NioEventLoopGroup(),
                    NioSocketChannel.class
            );
        }
    }

    /**
     * 服务端配置
     */
    public static class ServerConfig {
        public final EventLoopGroup bossGroup;
        public final EventLoopGroup workerGroup;
        public final Class<? extends ServerChannel> serverChannelClass;

        ServerConfig(EventLoopGroup bossGroup,
                     EventLoopGroup workerGroup,
                     Class<? extends ServerChannel> serverChannelClass) {
            this.bossGroup = bossGroup;
            this.workerGroup = workerGroup;
            this.serverChannelClass = serverChannelClass;
        }
    }

    /**
     * 客户端配置
     */
    public static class ClientConfig {
        public final EventLoopGroup workerGroup;
        public final Class<? extends Channel> clientChannelClass;

        ClientConfig(EventLoopGroup workerGroup,
                     Class<? extends Channel> clientChannelClass) {
            this.workerGroup = workerGroup;
            this.clientChannelClass = clientChannelClass;
        }
    }
}
