package com.xiaoniucode.etp.client;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.event.ApplicationInitEvent;
import com.xiaoniucode.etp.client.transport.CompressHandler;
import com.xiaoniucode.etp.client.transport.ControlFrameHandler;
import com.xiaoniucode.etp.client.transport.RealServerHandler;
import com.xiaoniucode.etp.client.listener.ApplicationInitListener;
import com.xiaoniucode.etp.client.manager.EventBusManager;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentStateMachineBuilder;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.client.transport.connection.DirectPool;
import com.xiaoniucode.etp.client.transport.connection.MultiplexPool;
import com.xiaoniucode.etp.core.codec.TMSPCodec;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.transport.IdleCheckHandler;
import com.xiaoniucode.etp.core.transport.compress.MultiplexSnappyDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.ssl.SslHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 代理客户端服务容器
 *
 * @author liuxin
 */
public final class TunnelClient implements Lifecycle {
    private final static Logger logger = LoggerFactory.getLogger(TunnelClient.class);
    private final AppConfig config;
    private EventLoopGroup controlWorkerGroup;
    private EventLoopGroup serverWorkBootstrap;
    private AgentContext clientContext;
    private StateMachine<AgentState, AgentEvent, AgentContext> stateMachine;

    public TunnelClient(AppConfig config) {
        this.config = config;
    }

    @Override
    public void start() {
        try {
            EventBusManager.register(new ApplicationInitListener());
            EventBusManager.publishAsync(new ApplicationInitEvent(config));
            initializeStateMachine();

            stateMachine.fireEvent(AgentState.IDLE, AgentEvent.START, clientContext);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void initializeStateMachine() {
        CompressHandler compressHandler = new CompressHandler();
        stateMachine = AgentStateMachineBuilder.getStateMachine();
        clientContext = new AgentContext(config);
        clientContext.setTunnelClient(this);
        clientContext.setStateMachine(stateMachine);
        clientContext.setDirectPool(new DirectPool());
        clientContext.setMultiplexPool(new MultiplexPool());

        serverWorkBootstrap = NettyEventLoopFactory.eventLoopGroup();
        Bootstrap serverBootstrap = new Bootstrap()
                .group(serverWorkBootstrap)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(64 * 1024, 256 * 1024))
//                .option(ChannelOption.SO_BACKLOG, 4096)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
//                        p.addLast(compressHandler);
                        p.addLast(NettyConstants.REAL_SERVER_HANDLER, new RealServerHandler(clientContext));
                    }
                });
        Bootstrap controlBootstrap = new Bootstrap();
        ControlFrameHandler controlTunnelHandler = new ControlFrameHandler(clientContext);
        controlWorkerGroup = NettyEventLoopFactory.eventLoopGroup();

        controlBootstrap.group(controlWorkerGroup)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
//                .option(ChannelOption.SO_BACKLOG, 4096)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(64 * 1024, 256 * 1024))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        if (Boolean.TRUE.equals(config.getTlsConfig().getEnable()) && clientContext.getTlsContext() != null) {
                            SslHandler sslHandler = clientContext.getTlsContext().newHandler(sc.alloc(), config.getServerAddr(), config.getServerPort());
                            sc.pipeline().addLast(NettyConstants.TLS_HANDLER, sslHandler);
                        }
                        sc.pipeline()
                                .addLast(NettyConstants.TMSP_CODEC, TMSPCodec.create(10 * 1024 * 1024))
                               // .addLast(new MultiplexSnappyDecoder())
                               // .addLast(NettyConstants.IDLE_CHECK_HANDLER, new IdleCheckHandler(60, 60, 0, TimeUnit.SECONDS))
                                .addLast(NettyConstants.CONTROL_FRAME_HANDLER, controlTunnelHandler);
                    }
                });

        clientContext.setControlBootstrap(controlBootstrap);
        clientContext.setControlWorkerGroup(controlWorkerGroup);
        clientContext.setServerBootstrap(serverBootstrap);
        clientContext.setServerWorkerGroup(serverWorkBootstrap);
    }

    @Override
    public void stop() {
        if (controlWorkerGroup != null) {
            controlWorkerGroup.shutdownGracefully();
        }
        if (serverWorkBootstrap != null) {
            serverWorkBootstrap.shutdownGracefully();
        }
        EventBusManager.shutdown();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Runtime.getRuntime().halt(0);
    }
}
