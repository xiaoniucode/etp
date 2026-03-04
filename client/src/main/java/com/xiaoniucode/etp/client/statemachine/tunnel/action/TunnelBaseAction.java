package com.xiaoniucode.etp.client.statemachine.tunnel.action;


import com.alibaba.cola.statemachine.Action;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.client.statemachine.tunnel.TunnelState;

public abstract class TunnelBaseAction implements Action<TunnelState, TunnelEvent, TunnelContext> {
    @Override
    public void execute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        context.setState(to);
        doExecute(from, to, event, context);
    }
    protected abstract void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context);
}
