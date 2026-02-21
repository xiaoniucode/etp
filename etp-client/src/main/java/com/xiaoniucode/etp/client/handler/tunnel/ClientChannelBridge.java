package com.xiaoniucode.etp.client.handler.tunnel;

import com.xiaoniucode.etp.core.handler.bridge.AbstractChannelBridge;
import io.netty.channel.Channel;

public class ClientChannelBridge extends AbstractChannelBridge {
    public ClientChannelBridge(Channel target, String direction) {
        super(target, direction);
    }
}
