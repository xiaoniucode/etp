package com.xiaoniucode.etp.server.statemachine.tunnel.action;

import com.alibaba.cola.statemachine.Action;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelEvent;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelState;

public abstract class TunnelBaseAction implements Action<TunnelState, TunnelEvent, TunnelContext> {

    @Override
    public final void execute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        try {
            context.setState(to);
            doExecute(from, to, event, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context);
}


