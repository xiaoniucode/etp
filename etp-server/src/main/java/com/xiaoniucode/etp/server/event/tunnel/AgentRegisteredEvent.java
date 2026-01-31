package com.xiaoniucode.etp.server.event.tunnel;

import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgentRegisteredEvent extends Event {
    private AgentSession agentSession;
}
