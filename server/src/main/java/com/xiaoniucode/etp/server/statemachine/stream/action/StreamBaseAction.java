package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.alibaba.cola.statemachine.Action;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StreamBaseAction implements Action<StreamState, StreamEvent, StreamContext> {
private final Logger logger= LoggerFactory.getLogger(StreamBaseAction.class);
    @Override
    public final void execute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        context.setState(to);
        try {
            doExecute(from, to, event, context);
        } catch (Exception e) {
            logger.error("执行错误",e);
        }
    }

    protected abstract void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context);
}