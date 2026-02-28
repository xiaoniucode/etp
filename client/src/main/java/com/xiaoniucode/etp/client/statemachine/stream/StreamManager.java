package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import io.netty.channel.Channel;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {
    private static final Map<Integer, StreamContext> streams = new ConcurrentHashMap<>(1024);

    public static StreamContext createStreamContext(Integer streamId, AgentContext agentContext) {
        if (streams.containsKey(streamId)) {
            throw new IllegalArgumentException("streamId 已经存在");
        }
        StateMachine<StreamState, StreamEvent, StreamContext> stateMachine = StreamStateMachineBuilder.buildStateMachine(streamId);
        StreamContext streamContext = new StreamContext(streamId, stateMachine, agentContext);

        streams.put(streamId, streamContext);
        return streamContext;
    }

    public static StreamContext getStreamContext(int streamId) {
        return streams.get(streamId);
    }

    public static Optional<StreamContext> getStreamContext(Channel visitor) {
        Integer streamId = visitor.attr(ChannelConstants.STREAM_ID).get();
        if (streamId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(streams.get(streamId));
    }
}
