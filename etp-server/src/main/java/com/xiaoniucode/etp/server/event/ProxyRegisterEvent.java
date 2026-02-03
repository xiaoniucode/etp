package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.notify.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProxyRegisterEvent extends Event {
   private final ProxyConfig proxyConfig;
}
