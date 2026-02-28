package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import org.springframework.stereotype.Component;

@Component
public class StreamDataAction extends StreamBaseAction {
    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {

    }
}
