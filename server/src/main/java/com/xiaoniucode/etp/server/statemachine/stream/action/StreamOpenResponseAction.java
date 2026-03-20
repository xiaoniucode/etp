package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.server.loadbalance.LeastConnHooks;
import com.xiaoniucode.etp.server.statemachine.stream.*;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.server.transport.bridge.TunnelBridge;
import com.xiaoniucode.etp.server.transport.bridge.TunnelBridgeFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 流打开成功处理
 */
@Component
public class StreamOpenResponseAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamOpenResponseAction.class);
    @Autowired
    private TunnelManager tunnelManager;
    @Autowired
    private LeastConnHooks leastConnHooks;

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        String tunnelId = context.getVariableAs(StreamConstants.TUNNEL_ID, String.class);
        Optional<TunnelContext> tc = tunnelManager.getTunnel(context.isMux(), tunnelId);
        if (tc.isPresent()) {
            Channel tunnel = tc.get().getTunnel();
            context.setTunnel(tunnel);
            Channel visitor = context.getVisitor();
            TunnelBridge tunnelBridge;
            if (context.isMux()) {
                tunnelBridge = TunnelBridgeFactory.buildMux(context);
                logger.debug("共享隧道建立成功: {}", context.getCurrentTarget());
            } else {
                logger.debug("独立隧道建立成功: {}", context.getCurrentTarget());
                tunnelBridge = TunnelBridgeFactory.buildDirect(context);
            }
            leastConnHooks.onStreamOpened(context);
            context.setWriteQueue(tc.get().getWriteQueue());
            //如果是 HTTP协议需要发送首次建立建立的时候读取到的第一个包
            if (context.getCurrentProtocol().isHttp()) {
                relayHttpFirstPackage(visitor, tunnelBridge);
            }
            visitor.config().setOption(ChannelOption.AUTO_READ, true);
        } else {
            // 打开失败也要收敛到 CLOSE，避免资源/计数泄漏
            context.fireEvent(StreamEvent.STREAM_OPEN_FAILURE);
            context.fireEvent(StreamEvent.STREAM_CLOSE);
        }
        context.removeVariable(StreamConstants.TUNNEL_ID);
    }

    /**
     * 发送HTTP 协议首次缓存的第一个数据包
     */
    public void relayHttpFirstPackage(Channel visitor, TunnelBridge tunnelBridge) {
        ByteBuf cached = visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).get();
        tunnelBridge.relayToTunnel(cached);
        visitor.attr(AttributeKeys.HTTP_FIRST_PACKET).set(null);
    }
}
