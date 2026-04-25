package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProxyUpdateEvent extends Event {
    private final AgentInfo agentInfo;
    private final ProxyConfig proxyConfig;
}
