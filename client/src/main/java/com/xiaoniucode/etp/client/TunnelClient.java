package com.xiaoniucode.etp.client;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.event.ApplicationInitEvent;
import com.xiaoniucode.etp.client.manager.AgentIdentity;
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
import com.xiaoniucode.etp.core.transport.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.server.Lifecycle;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * 代理客户端服务容器
 *
 * @author liuxin
 */
public final class TunnelClient implements Lifecycle {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(TunnelClient.class);
    private final AppConfig config;
    private EventLoopGroup controlWorkerGroup;
    private EventLoopGroup serverWorkBootstrap;
    private AgentContext agentContext;
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
            stateMachine.fireEvent(AgentState.IDLE, AgentEvent.START, agentContext);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void initializeStateMachine() {
        stateMachine = AgentStateMachineBuilder.getStateMachine();
        agentContext = new AgentContext(config);
        agentContext.setTunnelClient(this);
        agentContext.setStateMachine(stateMachine);
        agentContext.setDirectPool(new DirectPool());
        agentContext.setMultiplexPool(new MultiplexPool());
        agentContext.setAgentIdentity(new AgentIdentity());

        serverWorkBootstrap = NettyEventLoopFactory.eventLoopGroup();
        Bootstrap serverBootstrap = new Bootstrap()
                .group(serverWorkBootstrap)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(NettyConstants.REAL_SERVER_HANDLER, new RealServerHandler(agentContext));
                    }
                });
        Bootstrap controlBootstrap = new Bootstrap();
        ControlFrameHandler controlTunnelHandler = new ControlFrameHandler(agentContext);
        agentContext.setControlFrameHandler(controlTunnelHandler);
        controlWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        controlBootstrap.group(controlWorkerGroup)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        if (Boolean.TRUE.equals(config.getTlsConfig().getEnabled()) && agentContext.getTlsContext() != null) {
                            SslHandler sslHandler = agentContext.getTlsContext().newHandler(
                                    sc.alloc(), config.getServerAddr(), config.getServerPort());
                            sc.pipeline().addLast(NettyConstants.TLS_HANDLER, sslHandler);
                        }
                        sc.pipeline()
                                .addLast(loggingHandler)
                                .addLast(NettyConstants.TMSP_CODEC, TMSPCodec.create(10 * 1024 * 1024))
                                // .addLast(NettyConstants.IDLE_CHECK_HANDLER, new IdleCheckHandler(60, 60, 0, TimeUnit.SECONDS))
                                .addLast(NettyConstants.CONTROL_FRAME_HANDLER, controlTunnelHandler);
                    }
                });
        agentContext.setControlBootstrap(controlBootstrap);
        agentContext.setControlWorkerGroup(controlWorkerGroup);
        agentContext.setServerBootstrap(serverBootstrap);
        agentContext.setServerWorkerGroup(serverWorkBootstrap);
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
