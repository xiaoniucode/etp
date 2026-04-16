package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.metrics.MetricsCollector;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
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
    @Autowired
    private MetricsCollector metricsCollector;

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();
        Channel visitor = context.getVisitor();
        ProxyConfig proxyConfig = context.getProxyConfig();
        if (proxyConfig != null) {
            streamManager.decrementStreamCount(proxyConfig.getProxyId());
        }
        leastConnHooks.onStreamClosed(context);
        metricsCollector.onChannelInactive(context.getProxyId());

        ChannelUtils.closeOnFlush(visitor);
        AgentContext agentContext = context.getAgentContext();
        TunnelEntry tunnelEntry = context.getTunnelEntry();

        if (context.isDirectConnection() && tunnelEntry != null) {
            AgentInfo agentInfo = agentContext.getAgentInfo();
            logger.debug("回收客户端 {} 独立连接 {}", agentInfo.getAgentId(), tunnelEntry.getTunnelId());
            directPool.release(agentInfo.getAgentId(), tunnelEntry);
        }
        streamManager.removeStreamContext(streamId);
        if (event == StreamEvent.STREAM_LOCAL_CLOSE) {
            logger.debug("通知对端关闭流");
            Channel control = agentContext.getControl();
            TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_CLOSE);
            control.writeAndFlush(frame);
        }

        logger.debug("关闭流: streamId={}", context.getStreamId());
    }
}
