package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.config.AppConfig;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.client.transport.connection.DirectPool;
import com.xiaoniucode.etp.client.transport.connection.MultiplexPool;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.transport.NettyConstants;
import com.xiaoniucode.etp.core.transport.TlsHandlerCleanup;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelCreateRespAction extends AgentBaseAction {
    private static final Logger logger = LoggerFactory.getLogger(TunnelCreateRespAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext agentContext) {
        logger.debug("创建隧道成功");
        boolean encrypt = agentContext.getAndRemoveAs("encrypt", Boolean.class);
        boolean multiplex = agentContext.getAndRemoveAs("multiplex", Boolean.class);
        Message.TunnelCreateResponse resp = agentContext.getAndRemoveAs("tunnel_create_response", Message.TunnelCreateResponse.class);
        if (resp.getCode() == 1) {
            logger.info("隧道创建失败: {}", resp.getMessage());
            //todo 删除创建失败的隧道
        }
        TunnelEntry tunnelEntry;
        if (multiplex) {
            MultiplexPool multiplexPool = agentContext.getMultiplexPool();
            tunnelEntry = multiplexPool.activeTunnel(encrypt);
            if (tunnelEntry != null) {
                logger.debug("激活共享隧道: tunnelId={} 激活状态：{}", tunnelEntry.getTunnelId(), tunnelEntry.isActive());
                Channel tunnel = tunnelEntry.getChannel();
                ChannelPipeline tunnelPipeline = tunnel.pipeline();
                if (!encrypt && tunnelPipeline.get(NettyConstants.TLS_HANDLER) != null) {
                    TlsHandlerCleanup.removeTlsGracefully(tunnelPipeline);
                } else {
                    SslContext tlsContext = agentContext.getTlsContext();
                    if (tlsContext != null) {
                        AppConfig config = agentContext.getConfig();
                        SslHandler sslHandler = tlsContext.newHandler(tunnel.alloc(), config.getServerAddr(), config.getServerPort());
                        tunnelPipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
                    }
                }
            } else {
                logger.error("激活共享隧道失败，没有找到 {} 隧道", resp.getTunnelId());
            }
        } else {
            DirectPool directPool = agentContext.getDirectPool();
            tunnelEntry = directPool.activateTunnel(resp.getTunnelId());
            logger.debug("激活独立隧道: tunnelId={} 激活状态：{}", tunnelEntry.getTunnelId(), tunnelEntry.isActive());
        }
    }
}
