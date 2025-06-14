package cn.xilio.vine.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class VineConstants {
    public static final AttributeKey<Bootstrap> TUNNEL_BOOTSTRAP = AttributeKey.valueOf("tunnel_bootstrap");
    public static final AttributeKey<Bootstrap> REAL_BOOTSTRAP = AttributeKey.valueOf("real_bootstrap");
    public static final AttributeKey<Bootstrap> VISITOR_BOOTSTRAP = AttributeKey.valueOf("visitor_bootstrap");
    public static final AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");
}
