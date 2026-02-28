package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;
import io.netty.channel.Channel;

/**
 * 网络错误处理
 */
public class NetworkErrorAction extends AgentBaseAction {
    @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext context) {
        Channel control = context.getControl();
        control.close();

        context.fireEvent(ClientEvent.RETRY);
    }
}
