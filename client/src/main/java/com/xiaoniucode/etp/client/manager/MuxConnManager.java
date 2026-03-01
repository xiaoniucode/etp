package com.xiaoniucode.etp.client.manager;

import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端多路复用主连接管理器
 */
public class MuxConnManager {

    public enum ProtocolFeature {
        PLAIN, COMPRESS, ENCRYPT, ENCRYPT_COMPRESS
    }

    private final EventLoopGroup workerGroup = NettyEventLoopFactory.eventLoopGroup();
    private final Map<ProtocolFeature, Channel> muxChannels = new ConcurrentHashMap<>();
    private final Bootstrap bootstrap;
    private final String serverAddress;
    private final int serverPort;

    public MuxConnManager(Bootstrap bootstrap, String serverAddress, int serverPort) {
        this.bootstrap = bootstrap;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

    }

    public Channel getOrCreate(boolean compress, boolean useTls) {
        ProtocolFeature feature = toFeature(compress, useTls);
        return muxChannels.compute(feature, (f, existing) -> {
            if (existing != null && existing.isActive()) {
                return existing;
            }
            try {
                ChannelFuture channelFuture = bootstrap.connect(serverAddress, serverPort);
                Channel channel = channelFuture.channel();
                channel.closeFuture().addListener(fu -> muxChannels.remove(feature, channel));
                return channel;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private ProtocolFeature toFeature(boolean compress, boolean useTls) {
        if (useTls) return compress ? ProtocolFeature.ENCRYPT_COMPRESS : ProtocolFeature.ENCRYPT;
        return compress ? ProtocolFeature.COMPRESS : ProtocolFeature.PLAIN;
    }

    public void close(ProtocolFeature feature) {
        Channel ch = muxChannels.remove(feature);
        if (ch != null && ch.isActive()) ch.close();
    }

    public void shutdown() {
        muxChannels.values().forEach(ch -> {
            if (ch != null && ch.isActive()) ch.close();
        });
        muxChannels.clear();
        workerGroup.shutdownGracefully();
    }

    public int getActiveConnectionCount() {
        return (int) muxChannels.values().stream().filter(ch -> ch != null && ch.isActive()).count();
    }

    public boolean isActive(ProtocolFeature feature) {
        Channel ch = muxChannels.get(feature);
        return ch != null && ch.isActive();
    }
}