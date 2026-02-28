package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.alibaba.cola.statemachine.Action;
import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamState;

public abstract class StreamBaseAction implements Action<StreamState, StreamEvent, StreamContext> {
    @Override
    public void execute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        context.setState(to);
        doExecute(from, to, event, context);
    }
    protected abstract void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context);
}
