package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import com.xiaoniucode.etp.server.manager.domain.ProxyConfigExt;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewProxyRegisterEvent extends Event {
   private ProxyConfigExt proxyConfigExt;
}
