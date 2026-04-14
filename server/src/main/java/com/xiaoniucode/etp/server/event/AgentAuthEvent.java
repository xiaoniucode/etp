package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgentAuthEvent extends Event {
    /**
     * 客户端认证信息
     */
    private final AgentInfo agentInfo;
    /**
     * 是否是重连
     */
    private final boolean isReconnect;

}
