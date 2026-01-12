package com.xiaoniucode.etp.core.codec;

import io.netty.channel.CombinedChannelDuplexHandler;

public class TunnelMessageCodec extends CombinedChannelDuplexHandler<TunnelMessageDecoder, TunnelMessageEncoder> {
    public TunnelMessageCodec() {
        super(new TunnelMessageDecoder(), new TunnelMessageEncoder());
    }
}
