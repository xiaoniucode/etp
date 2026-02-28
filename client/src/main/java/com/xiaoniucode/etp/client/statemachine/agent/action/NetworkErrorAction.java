package com.xiaoniucode.etp.client.statemachine.agent.action;

import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.ClientEvent;
import com.xiaoniucode.etp.client.statemachine.agent.ClientState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络错误处理
 */
public class NetworkErrorAction extends AgentBaseAction {
    private final Logger logger= LoggerFactory.getLogger(NetworkErrorAction.class);
    @Override
    protected void doExecute(ClientState from, ClientState to, ClientEvent event, AgentContext context) {
        logger.error("网络错误");
        Channel control = context.getControl();
        control.close();

        context.fireEvent(ClientEvent.RETRY);
    }
}
