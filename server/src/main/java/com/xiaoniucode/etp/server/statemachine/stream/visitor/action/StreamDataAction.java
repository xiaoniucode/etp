package com.xiaoniucode.etp.server.statemachine.stream.visitor.action;

import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamState;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.StreamContext;
import org.springframework.stereotype.Component;

@Component
public class StreamDataAction extends StreamBaseAction {
    @Override
    protected void doExecute(ClientStreamState from, ClientStreamState to, ClientStreamEvent event, StreamContext context) {

    }
}
