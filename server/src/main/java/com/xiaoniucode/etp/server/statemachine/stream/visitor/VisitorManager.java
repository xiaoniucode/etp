package com.xiaoniucode.etp.server.statemachine.stream.visitor;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.server.configuration.StreamStateMachineFactory;
import com.xiaoniucode.etp.server.generator.StreamIdGenerator;


import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VisitorManager {
    /**
     * streamId -->连接上下文
     */
    private final Map<Integer, StreamContext> visitors = new ConcurrentHashMap<>();
    @Autowired
    private StreamIdGenerator streamIdGenerator;
    @Autowired
    private StreamStateMachineFactory streamStateMachineFactory;

    /**
     * 创建并保存新的流上下文
     */
    public StreamContext createStreamContext(Channel visitor) {
        int streamId = streamIdGenerator.nextStreamId();
        StateMachine<ClientStreamState, ClientStreamEvent, StreamContext> stateMachine = streamStateMachineFactory.create(streamId);
        StreamContext streamContext = new StreamContext(streamId, stateMachine);
        streamContext.setVisitor(visitor);
        streamContext.setSourceAddress(getRemoteAddress(visitor));
        visitor.attr(ChannelConstants.STREAM_ID).set(streamId);
        visitors.put(streamId, streamContext);
        return streamContext;
    }


    /**
     * 获取客户端远程地址
     */
    private String getRemoteAddress(Channel visitor) {
        try {
            return visitor.remoteAddress().toString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public StreamContext getServerStreamContext(Integer streamId) {
        return visitors.get(streamId);
    }

    /**
     * 判断是否存在
     */
    public boolean containsStream(Integer streamId) {
        return visitors.containsKey(streamId);
    }

    /**
     * 获取所有流数量
     */
    public int getStreamCount() {
        return visitors.size();
    }

    public Optional<StreamContext> getStreamContext(Channel visitor) {
        Integer streamId = visitor.attr(ChannelConstants.STREAM_ID).get();
        if (streamId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(visitors.get(streamId));
    }
}
