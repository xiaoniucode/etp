package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.server.loadbalance.LeastConnHooks;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.stream.*;
import com.xiaoniucode.etp.server.transport.connection.DirectPool;
import com.xiaoniucode.etp.server.transport.connection.MultiplexPool;
import com.xiaoniucode.etp.core.transport.TunnelBridge;
import com.xiaoniucode.etp.server.transport.bridge.TunnelBridgeFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 流打开成功处理
 */
@Component
public class StreamOpenResponseAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamOpenResponseAction.class);
    @Autowired
    private DirectPool directPool;
    @Autowired
    private MultiplexPool multiplexPool;
    @Autowired
    private LeastConnHooks leastConnHooks;

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        logger.debug("收到流 {} 打开通知", context.getStreamId());
        String tunnelId = context.getAndRemoveAs(StreamConstants.TUNNEL_ID, String.class);
        AgentContext agentContext = context.getAgentContext();
        String clientId = agentContext.getClientId();
        TunnelEntry tunnelEntry;
        if (context.isMultiplex()) {
            tunnelEntry = multiplexPool.acquire(clientId, context.isEncrypt());
        } else {
            tunnelEntry = directPool.borrow(clientId, tunnelId);
        }
        if (tunnelEntry==null){
            throw new IllegalArgumentException("tunnelEntry 不能为空");
        }
        context.setTunnelEntry(tunnelEntry);
        Channel visitor = context.getVisitor();
        TunnelBridge tunnelBridge;
        if (context.isMultiplex()) {
            tunnelBridge = TunnelBridgeFactory.buildMux(context);
            logger.debug("共享隧道建立成功，访问目标: {}", context.getCurrentTarget());
        } else {
            tunnelBridge = TunnelBridgeFactory.buildDirect(context);
            logger.debug("独立隧道建立成功，隧道ID: {}", tunnelEntry.getTunnelId());
        }
        tunnelBridge.open();
        context.setTunnelBridge(tunnelBridge);
        leastConnHooks.onStreamOpened(context);
        //如果是 HTTP协议需要发送首次建立建立的时候读取到的第一个包
        if (context.getCurrentProtocol().isHttp()) {
            relayHttpFirstPackage(visitor, tunnelBridge);
        }
        visitor.config().setOption(ChannelOption.AUTO_READ, true);
        logger.debug("流 {} 打开成功，可以从访问者读数据", context.getStreamId());
    }

    /**
     * 发送HTTP 协议首次缓存的第一个数据包
     */
    public void relayHttpFirstPackage(Channel visitor, TunnelBridge tunnelBridge) {
        ByteBuf cached = visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).get();
        tunnelBridge.forwardToLocal(cached);
        visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).set(null);
    }
}
