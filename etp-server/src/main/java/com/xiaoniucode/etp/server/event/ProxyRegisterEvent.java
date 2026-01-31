package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProxyRegisterEvent extends Event {
   private ProxyConfig proxyConfig;
}
