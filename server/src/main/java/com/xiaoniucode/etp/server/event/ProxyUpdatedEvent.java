package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.core.notify.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProxyUpdatedEvent extends Event {
    private final String agentId;
    private final AgentType clientType;
    private final ProxyConfig proxyConfig;
}
