package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.statemachine.TunnelType;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 预创建数据传输隧道
 * 多路复用隧道 加密+压缩排列组合
 * 独立隧道连接池
 */
public class CreateTunnelPoolAction extends AgentBaseAction {
    private final int DEFAULT_DIRECT_COUNT = 6;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Integer connectionId = context.getConnectionId();
        Bootstrap bootstrap = context.getControlBootstrap();
        AppConfig config = context.getConfig();

        //创建多路复用隧道
        //如果配置了TLS证书可创建加密隧道
        //TLS+压缩 空 TLS+空 压缩+空

        //创建独立连接池隧道
        for (int i = 0; i < 4; i++) {
            TunnelContext tunnelContext = TunnelManager.createTunnelContext(connectionId);
            tunnelContext.setConnectionId(connectionId);
            bootstrap.connect(config.getServerAddr(), config.getServerPort()).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    tunnelContext.fireEvent(TunnelEvent.CLOSE);
                } else {
                    Channel tunnel = future.channel();
                    Message.TunnelCreateRequest body = Message.TunnelCreateRequest.newBuilder()
                            .setTunnelId(tunnelContext.getTunnelId())
                            .build();
                    ByteBuf payload = ProtobufUtil.toByteBuf(body, tunnel.alloc());
                    TMSPFrame frame = new TMSPFrame(connectionId, TMSP.MSG_TUNNEL_CREATE, payload);
                    frame.setMuxTunnel(false);

                    tunnel.writeAndFlush(frame).addListener((ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            tunnelContext.setTunnelType(TunnelType.DIRECT);
                            tunnelContext.setTunnel(tunnel);
                            tunnelContext.fireEvent(TunnelEvent.CONNECT);
                        } else {
                            tunnel.close();
                            tunnelContext.fireEvent(TunnelEvent.CLOSE);
                        }
                    });
                }
            });
        }

    }
    public void createDirectTunnel(AgentContext context){
        Integer connectionId = context.getConnectionId();
        Bootstrap bootstrap = context.getControlBootstrap();
        AppConfig config = context.getConfig();
        for (int i = 0; i < DEFAULT_DIRECT_COUNT; i++) {
            TunnelContext tunnelContext = TunnelManager.createTunnelContext(connectionId);
            tunnelContext.setConnectionId(connectionId);
            bootstrap.connect(config.getServerAddr(), config.getServerPort()).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    tunnelContext.fireEvent(TunnelEvent.CLOSE);
                } else {
                    Channel tunnel = future.channel();
                    Message.TunnelCreateRequest body = Message.TunnelCreateRequest.newBuilder()
                            .setTunnelId(tunnelContext.getTunnelId())
                            .build();
                    ByteBuf payload = ProtobufUtil.toByteBuf(body, tunnel.alloc());
                    TMSPFrame frame = new TMSPFrame(connectionId, TMSP.MSG_TUNNEL_CREATE, payload);
                    frame.setMuxTunnel(false);

                    tunnel.writeAndFlush(frame).addListener((ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            tunnelContext.setTunnelType(TunnelType.DIRECT);
                            tunnelContext.setTunnel(tunnel);
                            tunnelContext.fireEvent(TunnelEvent.CONNECT);
                        } else {
                            tunnel.close();
                            tunnelContext.fireEvent(TunnelEvent.CLOSE);
                        }
                    });
                }
            });
        }
    }
}
