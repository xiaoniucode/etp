package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.alibaba.cola.statemachine.Action;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;

public abstract class AgentBaseAction implements Action<ClientState, ClientEvent, AgentContext> {
    @Override
    public void execute(ClientState from, ClientState to, ClientEvent event, AgentContext context) {
        context.setState(to);
        doExecute(from, to, event, context);
    }

    protected abstract void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext context);
}
