package com.xiaoniucode.etp.client;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.event.ApplicationInitEvent;
import com.xiaoniucode.etp.client.transport.ControlFrameHandler;
import com.xiaoniucode.etp.client.transport.RealServerHandler;
import com.xiaoniucode.etp.client.helper.TunnelClientHelper;
import com.xiaoniucode.etp.client.listener.ApplicationInitListener;
import com.xiaoniucode.etp.client.manager.EventBusManager;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentStateMachineBuilder;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;
import com.xiaoniucode.etp.core.codec.TMSPCodec;
import com.xiaoniucode.etp.core.factory.NettyEventLoopFactory;
import com.xiaoniucode.etp.core.server.Lifecycle;
import com.xiaoniucode.etp.core.netty.IdleCheckHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
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
    private StateMachine<ClientState, ClientEvent, AgentContext> stateMachine;

    public TunnelClient(AppConfig config) {
        this.config = config;
    }

    @Override
    public void start() {
        try {
            EventBusManager.register(new ApplicationInitListener());
            EventBusManager.publishAsync(new ApplicationInitEvent(config));
            TunnelClientHelper.setTunnelClient(this);

            initializeStateMachine();

            stateMachine.fireEvent(ClientState.INITIALIZED, ClientEvent.START, clientContext);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void initializeStateMachine() {
        stateMachine = AgentStateMachineBuilder.buildStateMachine();
        stateMachine.showStateMachine();
        clientContext = new AgentContext(config);
        clientContext.setStateMachine(stateMachine);
        serverWorkBootstrap = NettyEventLoopFactory.eventLoopGroup();
        Bootstrap serverBootstrap = new Bootstrap()
                .group(serverWorkBootstrap)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);

        Bootstrap controlBootstrap = new Bootstrap();
        ControlFrameHandler controlTunnelHandler = new ControlFrameHandler(clientContext);
        controlWorkerGroup = NettyEventLoopFactory.eventLoopGroup();
        controlBootstrap.group(controlWorkerGroup)
                .channel(NettyEventLoopFactory.socketChannelClass())
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(256 * 1024, 4 * 1024 * 1024))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) {
                        if (config.getTlsConfig().getEnable() && clientContext.getTlsContext() != null) {
                            SslHandler sslHandler = clientContext.getTlsContext().newHandler(sc.alloc());
                            sc.pipeline().addLast("tls", sslHandler);
                        }
                        sc.pipeline()
                                .addLast(new TMSPCodec.Decoder(10 * 1024 * 1024))
                                .addLast(new TMSPCodec.Encoder())
                                .addLast("idleCheckHandler", new IdleCheckHandler(30, 30, 0, TimeUnit.SECONDS))
                                .addLast("controlTunnelHandler", controlTunnelHandler);
                    }
                });

        serverBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new RealServerHandler(clientContext));
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
    }
}
