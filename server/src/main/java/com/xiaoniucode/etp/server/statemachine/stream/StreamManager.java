package com.xiaoniucode.etp.server.statemachine.stream;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.core.netty.AttributeKeys;
import com.xiaoniucode.etp.server.generator.StreamIdGenerator;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamManager {
    private final Logger logger = LoggerFactory.getLogger(StreamManager.class);
    /**
     * streamId -->连接上下文
     */
    private final Map<Integer, StreamContext> visitors = new ConcurrentHashMap<>();
    @Autowired
    private StreamIdGenerator streamIdGenerator;

    /**
     * 创建并保存新的流上下文
     */
    public StreamContext createStreamContext(Channel visitor, StateMachine<StreamState, StreamEvent, StreamContext> stateMachine) {
        int streamId = streamIdGenerator.nextStreamId();
        StreamContext streamContext = new StreamContext(streamId, stateMachine);
        streamContext.setVisitor(visitor);
        streamContext.setSourceAddress(getRemoteAddress(visitor));
        visitor.attr(AttributeKeys.STREAM_ID).set(streamId);
        visitors.put(streamId, streamContext);
        return streamContext;
    }

    public boolean removeStreamContext(int streamId) {
        StreamContext remove = visitors.remove(streamId);
        if (remove == null) {
            logger.error("流上下文删除失败，未找到上下文信息 streamId={}", streamId);
        }
        return remove != null;
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

    public StreamContext getStreamContext(Integer streamId) {
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
        Integer streamId = visitor.attr(AttributeKeys.STREAM_ID).get();
        if (streamId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(visitors.get(streamId));
    }

    public void closeStreams(Integer remotePort) {

    }

    public void closeStreams(Set<String> domains) {

    }

    public void closeStreams(String domain) {

    }
}
