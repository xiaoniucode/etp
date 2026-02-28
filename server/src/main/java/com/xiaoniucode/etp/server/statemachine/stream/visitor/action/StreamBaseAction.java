package com.xiaoniucode.etp.server.statemachine.stream.visitor.action;

import com.alibaba.cola.statemachine.Action;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamState;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.StreamContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StreamBaseAction implements Action<ClientStreamState, ClientStreamEvent, StreamContext> {
private final Logger logger= LoggerFactory.getLogger(StreamBaseAction.class);
    @Override
    public final void execute(ClientStreamState from, ClientStreamState to, ClientStreamEvent event, StreamContext context) {
        context.setState(to);
        try {
            doExecute(from, to, event, context);
        } catch (Exception e) {
            logger.error("执行错误",e);
        }
    }

    protected abstract void doExecute(ClientStreamState from, ClientStreamState to, ClientStreamEvent event, StreamContext context);
}