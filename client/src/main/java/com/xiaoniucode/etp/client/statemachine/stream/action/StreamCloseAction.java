package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.client.statemachine.stream.StreamState;
import com.xiaoniucode.etp.client.transport.connection.DirectPool;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.transport.TunnelEntry;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class StreamCloseAction extends StreamBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(StreamCloseAction.class);

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();

        Channel server = context.getServer();
        TunnelEntry tunnelEntry = context.getTunnelEntry();
        ChannelUtils.closeOnFlush(server);
        AgentContext agentContext = (AgentContext) context.getAgentContext();
        DirectPool directPool = agentContext.getDirectPool();
        directPool.release(tunnelEntry);

        StreamManager.removeStreamContext(streamId);
        if (event == StreamEvent.STREAM_LOCAL_CLOSE) {
            logger.debug("通知对端关闭流");
            context.getControl().writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_STREAM_CLOSE));
        }
        logger.debug("隧道关闭 - localIp={} localPort={}", context.getLocalIp(), context.getLocalPort());
    }
}
