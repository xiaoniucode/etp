package com.xiaoniucode.etp.client.transport;

import com.xiaoniucode.etp.core.netty.bridge.AbstractChannelBridge;
import io.netty.channel.Channel;

public class ClientChannelBridge extends AbstractChannelBridge {
    public ClientChannelBridge(Channel target, String direction) {
        super(target, direction);
    }
}
