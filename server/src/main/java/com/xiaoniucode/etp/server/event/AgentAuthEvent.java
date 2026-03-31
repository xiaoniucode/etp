package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import lombok.Getter;

@Getter
public class AgentAuthEvent extends Event {
//    private final String clientId;
//    private final String name;
//    private final ClientType clientType;
//    private final String token;
//    private final String arch;
//    private final String os;
//    private final String version;

    public AgentAuthEvent(AgentContext context) {

    }
}
