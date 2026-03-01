package com.xiaoniucode.etp.client.statemachine.tunnel;

public class TunnelCreateRespAction extends TunnelBaseAction{
    @Override
    protected void doExecute(TunnelState from, TunnelState to, TunnelEvent event, TunnelContext context) {
        int tunnelId = context.getTunnelId();
    }
}
