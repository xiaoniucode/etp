package com.xiaoniucode.etp.server.event;

import com.xiaoniucode.etp.core.notify.Event;
import lombok.Getter;

@Getter
public class TunnelServerStartingEvent extends Event {
    public TunnelServerStartingEvent() {
        super();
    }
}
