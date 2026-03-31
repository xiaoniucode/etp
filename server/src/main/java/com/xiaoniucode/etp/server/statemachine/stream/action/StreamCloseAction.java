package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.config.domain.AgentInfo;
import com.xiaoniucode.etp.server.loadbalance.LeastConnHooks;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.transport.connection.DirectPool;
import com.xiaoniucode.etp.server.transport.connection.MultiplexPool;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamCloseAction extends StreamBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(StreamCloseAction.class);
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private LeastConnHooks leastConnHooks;
    @Autowired
    private DirectPool directPool;
    @Autowired
    private MultiplexPool multiplexPool;

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();
        Channel visitor = context.getVisitor();
        leastConnHooks.onStreamClosed(context);
        ChannelUtils.closeOnFlush(visitor);
        AgentContext agentContext = context.getAgentContext();
        TunnelEntry tunnelEntry = context.getTunnelEntry();
        if (tunnelEntry != null) {
            Channel tunnel = tunnelEntry.getChannel();
            logger.debug("隧道 {} 激活状态：{}，隧道可写状态：{}", tunnelEntry.getTunnelId(), tunnel.isActive(), tunnel.isWritable());
            tunnel.config().setAutoRead(true);
        }
        if (!context.isMultiplex() && tunnelEntry != null) {
            AgentInfo agentInfo = agentContext.getAgentInfo();
            logger.debug("回收客户端 {} 独立连接 {}", agentInfo.getAgentId(), tunnelEntry.getTunnelId());
            directPool.release(agentInfo.getAgentId(), tunnelEntry);
        }

        streamManager.decrementStreamCount(context.getProxyConfig().getProxyId());

        streamManager.removeStreamContext(streamId);
        if (agentContext != null) {
            Channel control = agentContext.getControl();
            TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_CLOSE);
            control.writeAndFlush(frame);
        }

        logger.debug("关闭流: streamId={}", context.getStreamId());
    }
}
