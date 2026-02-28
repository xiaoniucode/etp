package com.xiaoniucode.etp.server.configuration;

import com.alibaba.cola.statemachine.StateMachine;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamStateMachineHolder {
    private static StreamStateMachineConfig config;

    @Autowired
    public void setConfig(StreamStateMachineConfig config) {
        StreamStateMachineHolder.config = config;
    }

    public static StateMachine<StreamState, StreamEvent, StreamContext> get(int streamId) {
        return config.create("stream:" + streamId);
    }
}
