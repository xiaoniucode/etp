package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.loadbalance.LeastConnHooks;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamCloseAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamCloseAction.class);
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private LeastConnHooks leastConnHooks;

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();
        Channel visitor = context.getVisitor();
        leastConnHooks.onStreamClosed(context);
        ChannelUtils.closeOnFlush(visitor);
        AgentContext agentContext = context.getAgentContext();
        streamManager.removeStreamContext(streamId);
        if (agentContext != null) {
            Channel control = agentContext.getControl();
            TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_CLOSE);
            control.writeAndFlush(frame);
        }

        logger.debug("关闭隧道: streamId={}", context.getStreamId());
    }
}
