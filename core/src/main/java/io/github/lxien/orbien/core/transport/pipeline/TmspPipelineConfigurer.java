package io.github.lxien.orbien.core.transport.pipeline;

import io.github.lxien.orbien.core.codec.TMSPCodec;
import io.github.lxien.orbien.core.domain.TlsConfig;
import io.github.lxien.orbien.core.domain.transport.WebSocketProtocolConfig;
import io.github.lxien.orbien.core.transport.AttributeKeys;
import io.github.lxien.orbien.core.transport.NettyConstants;
import io.github.lxien.orbien.core.transport.websocket.WebSocketBinaryFrameCodec;
import io.github.lxien.orbien.core.transport.api.PipelineRole;
import io.github.lxien.orbien.core.enums.TransportProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.net.URI;

public final class TmspPipelineConfigurer {

    private static final int DEFAULT_MAX_FRAME = 10 * 1024 * 1024;
    /**
     * Netty 默认 high=64KiB，与常见 64KiB STREAM_DATA 叠加 WS+TLS 后几乎每帧都触发不可写
     * 适度抬高到 128/256KiB：既减少 writability 抖动，又避免 1MiB 水位在限流抖动时灌爆 direct memory
     */
    private static final WriteBufferWaterMark WEBSOCKET_WRITE_BUFFER_WATER_MARK =
            new WriteBufferWaterMark(128 * 1024, 256 * 1024);

    private TmspPipelineConfigurer() {
    }

    public static void configureClientPipeline(Channel channel,
                                               TransportProtocol protocol,
                                               PipelineRole role,
                                               TlsConfig tlsConfig,
                                               SslContext sslContext,
                                               WebSocketProtocolConfig wsConfig,
                                               String remoteHost,
                                               int remotePort,
                                               boolean connectionEncrypt) {
        ChannelPipeline pipeline = channel.pipeline();
        switch (protocol) {
            case TCP -> addTcpClient(pipeline, tlsConfig, sslContext, remoteHost, remotePort, connectionEncrypt);
            case WEBSOCKET -> {
                addWebSocketClient(pipeline, sslContext, wsConfig, remoteHost, remotePort);
                channel.config().setOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WEBSOCKET_WRITE_BUFFER_WATER_MARK);
            }
            case QUIC -> {
                // QUIC TLS 由连接层处理
            }
            default -> throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
        addTmspStack(pipeline);
        channel.attr(AttributeKeys.TRANSPORT_PROTOCOL).set(protocol);
    }

    public static void configureServerPipeline(Channel channel,
                                               TransportProtocol protocol,
                                               TlsConfig tlsConfig,
                                               SslContext sslContext,
                                               WebSocketProtocolConfig wsConfig) {
        ChannelPipeline pipeline = channel.pipeline();
        switch (protocol) {
            case TCP -> addTcpServer(pipeline, sslContext);
            case WEBSOCKET -> {
                addWebSocketServer(pipeline, sslContext, wsConfig);
                channel.config().setOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WEBSOCKET_WRITE_BUFFER_WATER_MARK);
            }
            case QUIC -> {
                // QUIC stream pipeline
            }
            default -> throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
        addTmspStack(pipeline);
        channel.attr(AttributeKeys.TRANSPORT_PROTOCOL).set(protocol);
    }

    private static void addTcpClient(ChannelPipeline pipeline,
                                     TlsConfig tlsConfig,
                                     SslContext sslContext,
                                     String remoteHost,
                                     int remotePort,
                                     boolean connectionEncrypt) {
        if (connectionEncrypt && tlsConfig != null && tlsConfig.isEnabled() && sslContext != null) {
            int sniPort = remotePort > 0 ? remotePort : 443;
            SslHandler sslHandler = sslContext.newHandler(pipeline.channel().alloc(), remoteHost, sniPort);
            pipeline.addLast(NettyConstants.TLS_HANDLER, sslHandler);
        }
    }

    private static void addTcpServer(ChannelPipeline pipeline, SslContext sslContext) {
        if (sslContext != null) {
            pipeline.addLast(NettyConstants.TLS_HANDLER, sslContext.newHandler(pipeline.channel().alloc()));
        }
    }

    private static void addWebSocketClient(ChannelPipeline pipeline,
                                           SslContext sslContext,
                                           WebSocketProtocolConfig wsConfig,
                                           String remoteHost,
                                           int remotePort) {
        if (sslContext == null) {
            throw new IllegalStateException("WebSocket 传输必须启用 TLS");
        }
        if (remotePort <= 0) {
            throw new IllegalArgumentException("WebSocket 远程端口无效: " + remotePort);
        }
        int maxFrame = resolveMaxFrame(wsConfig);
        String path = normalizeWebSocketPath(wsConfig != null ? wsConfig.getPath() : null);
        pipeline.addLast(NettyConstants.TLS_HANDLER,
                sslContext.newHandler(pipeline.channel().alloc(), remoteHost, remotePort));
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(maxFrame));
        URI wsUri = buildWebSocketUri(remoteHost, remotePort, path);
        WebSocketClientProtocolHandler wsHandler = new WebSocketClientProtocolHandler(
                wsUri,
                WebSocketVersion.V13,
                null,
                false,
                null,
                maxFrame
        );
        pipeline.addLast(NettyConstants.WEBSOCKET_HANDLER, wsHandler);
        pipeline.addLast(NettyConstants.WEBSOCKET_FRAME_AGGREGATOR, new WebSocketFrameAggregator(maxFrame));
        pipeline.addLast(NettyConstants.WEBSOCKET_FRAME_CODEC, new WebSocketBinaryFrameCodec());
    }

    private static void addWebSocketServer(ChannelPipeline pipeline,
                                           SslContext sslContext,
                                           WebSocketProtocolConfig wsConfig) {
        if (sslContext == null) {
            throw new IllegalStateException("WebSocket 传输必须启用 TLS");
        }
        int maxFrame = resolveMaxFrame(wsConfig);
        String path = normalizeWebSocketPath(wsConfig != null ? wsConfig.getPath() : null);
        pipeline.addLast(NettyConstants.TLS_HANDLER, sslContext.newHandler(pipeline.channel().alloc()));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(maxFrame));
        pipeline.addLast(NettyConstants.WEBSOCKET_HANDLER,
                new WebSocketServerProtocolHandler(path, null, false, maxFrame));
        pipeline.addLast(NettyConstants.WEBSOCKET_FRAME_AGGREGATOR, new WebSocketFrameAggregator(maxFrame));
        pipeline.addLast(NettyConstants.WEBSOCKET_FRAME_CODEC, new WebSocketBinaryFrameCodec());
    }

    private static void addTmspStack(ChannelPipeline pipeline) {
        pipeline.addLast(NettyConstants.TMSP_CODEC, TMSPCodec.create(DEFAULT_MAX_FRAME));
    }

    private static int resolveMaxFrame(WebSocketProtocolConfig wsConfig) {
        if (wsConfig == null || wsConfig.getMaxFrameSize() <= 0) {
            return DEFAULT_MAX_FRAME;
        }
        return wsConfig.getMaxFrameSize();
    }

    static URI buildWebSocketUri(String host, int port, String path) {
        String normalizedPath = normalizeWebSocketPath(path);
        return URI.create("wss://" + host + ":" + port + normalizedPath);
    }

    static String normalizeWebSocketPath(String path) {
        if (path == null || path.isBlank()) {
            return "/tunnel";
        }
        String trimmed = path.trim();
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }
}
