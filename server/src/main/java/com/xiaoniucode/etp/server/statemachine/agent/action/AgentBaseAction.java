package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.alibaba.cola.statemachine.Action;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.agent.AgentState;
import com.xiaoniucode.etp.server.statemachine.agent.AgentEvent;

public abstract class AgentBaseAction implements Action<AgentState, AgentEvent, AgentContext> {

    @Override
    public final void execute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        try {
            context.setState(to);
            doExecute(from, to, event, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context);
}