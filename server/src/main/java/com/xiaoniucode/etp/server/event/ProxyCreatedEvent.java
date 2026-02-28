package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.notify.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProxyCreatedEvent extends Event {
    private final String clientId;
    private final ClientType clientType;
    private final ProxyConfig proxyConfig;
}
