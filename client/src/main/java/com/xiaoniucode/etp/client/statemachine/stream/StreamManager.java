package com.xiaoniucode.etp.client.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.core.transport.AttributeKeys;
import com.xiaoniucode.etp.core.transport.IntSet;
import com.xiaoniucode.etp.core.transport.PausedStreamRegistry;
import io.netty.channel.Channel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {
    private static final Map<Integer, StreamContext> streams = new ConcurrentHashMap<>(1024);
    private static final PausedStreamRegistry pausedStreamRegistry = new PausedStreamRegistry();

    public static StreamContext createStreamContext(Integer streamId, AgentContext agentContext) {
        if (streams.containsKey(streamId)) {
            throw new IllegalArgumentException("streamId 已经存在");
        }
        StateMachine<StreamState, StreamEvent, StreamContext> stateMachine = StreamStateMachineBuilder.getStateMachine();
        StreamContext streamContext = new StreamContext(streamId, stateMachine, agentContext);

        streams.put(streamId, streamContext);
        return streamContext;
    }

    public static Optional<StreamContext> getStreamContext(int streamId) {
        return Optional.ofNullable(streams.get(streamId));
    }

    public static Optional<StreamContext> getStreamContext(Channel visitor) {
        Integer streamId = visitor.attr(AttributeKeys.STREAM_ID).get();
        if (streamId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(streams.get(streamId));
    }

    public static boolean removeStreamContext(int streamId) {
        return streams.remove(streamId) != null;
    }

    public static void addPausedStreamId(Channel tunnel, int streamId) {
        pausedStreamRegistry.addPausedStreamId(tunnel, streamId);
    }

    public static IntSet getPausedStreamIds(Channel tunnel) {
        return pausedStreamRegistry.getPausedStreamIds(tunnel);
    }

    public static void removePausedStream(Channel tunnel, int streamId) {
        pausedStreamRegistry.removePausedStream(tunnel, streamId);
    }
    public static Collection<StreamContext> getStreamContexts(){
       return streams.values();
    }
}
