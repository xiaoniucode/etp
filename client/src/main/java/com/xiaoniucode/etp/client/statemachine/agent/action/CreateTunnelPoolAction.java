package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.client.statemachine.stream.TunnelConfig;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 预创建数据传输隧道
 * 多路复用隧道 加密+压缩排列组合
 * 独立隧道连接池
 */
public class CreateTunnelPoolAction extends AgentBaseAction {
    private static final int DEFAULT_DIRECT_COUNT = 100;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        createMuxTunnels(context);
        createDirectTunnels(context);
    }

    /**
     * 创建多路复用隧道
     */
    private void createMuxTunnels(AgentContext context) {
        List<TunnelConfig> muxTunnelConfigs = generateMuxTunnelConfigs(context);
        for (TunnelConfig tunnelConfig : muxTunnelConfigs) {
            createTunnel(context, tunnelConfig, true);
        }
    }

    /**
     * 创建独立隧道
     */
    private void createDirectTunnels(AgentContext context) {
        for (int i = 0; i < DEFAULT_DIRECT_COUNT; i++) {
            createTunnel(context, new TunnelConfig(false, context.getTlsContext() != null, false), false);
        }
    }

    /**
     * 生成多路复用隧道配置列表
     */
    private List<TunnelConfig> generateMuxTunnelConfigs(AgentContext context) {
        List<TunnelConfig> configs = new ArrayList<>();
        configs.add(new TunnelConfig(true, false, false));
        configs.add(new TunnelConfig(true, false, true));
        if (context.getTlsContext() != null) {
            configs.add(new TunnelConfig(true, true, true));
            configs.add(new TunnelConfig(true, true, false));
        }
        return configs;
    }

    private void createTunnel(AgentContext agentContext, TunnelConfig tunnelConfig, boolean isMux) {
        Integer connectionId = agentContext.getConnectionId();
        Bootstrap bootstrap = agentContext.getControlBootstrap();
        AppConfig config = agentContext.getConfig();

        TunnelContext tunnelContext = TunnelManager.createTunnelContext(agentContext, connectionId, isMux);
        tunnelContext.setConnectionId(connectionId);

        bootstrap.connect(config.getServerAddr(), config.getServerPort()).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                tunnelContext.fireEvent(TunnelEvent.CLOSE);
                return;
            }

            Channel tunnel = future.channel();
            Message.TunnelCreateRequest body = Message.TunnelCreateRequest.newBuilder()
                    .setTunnelId(tunnelContext.getTunnelId())
                    .build();
            ByteBuf payload = ProtobufUtil.toByteBuf(body, tunnel.alloc());
            TMSPFrame frame = new TMSPFrame(connectionId, TMSP.MSG_TUNNEL_CREATE, payload);

            frame.setMuxTunnel(isMux);
            frame.setEncrypted(tunnelConfig.isEncrypt());
            frame.setCompressed(tunnelConfig.isCompress());

            tunnel.writeAndFlush(frame).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    tunnelContext.setMux(isMux);
                    tunnelContext.setTunnel(tunnel);
                    tunnelContext.fireEvent(TunnelEvent.CONNECT);
                } else {
                    ReferenceCountUtil.release(payload);
                    tunnel.close();
                    tunnelContext.fireEvent(TunnelEvent.CLOSE);
                }
            });
        });
    }
}
