package com.xiaoniucode.etp.core;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;

/**
 * 通道相关常量
 * @author liuxin
 */
public class EtpConstants {
    public static final AttributeKey<Long> SESSION_ID = AttributeKey.valueOf("etp.sessionId");
    public static final AttributeKey<String> SECRET_KEY = AttributeKey.valueOf("etp.secretKey");

    public static final AttributeKey<Channel> DATA_CHANNEL = AttributeKey.valueOf("etp.dataChannel");
    public static final AttributeKey<Channel> CONTROL_CHANNEL = AttributeKey.valueOf("etp.controlChannel");
    public static final AttributeKey<Channel> REAL_SERVER_CHANNEL = AttributeKey.valueOf("etp.realServerChannel");
    public static final AttributeKey<Channel> VISITOR_CHANNEL = AttributeKey.valueOf("etp.visitorChannel");
    public static final AttributeKey<Boolean> CONNECTED = AttributeKey.valueOf("etp.connected");

    public static final AttributeKey<Map<Long, Channel>> VISITOR_CHANNELS = AttributeKey.newInstance("etp.visitorChannels");
    public static final AttributeKey<List<Integer>> CHANNEL_REMOTE_PORT = AttributeKey.valueOf("etp.channelRemotePort");

    public static final AttributeKey<String> SERVER_DDR = AttributeKey.valueOf("etp.serverAddr");
    public static final AttributeKey<Integer> SERVER_PORT = AttributeKey.valueOf("etp.serverPort");
    public static final AttributeKey<Integer> TARGET_PORT = AttributeKey.valueOf("etp.targetPort");
    public static final AttributeKey<Integer> PROXY_ID = AttributeKey.valueOf("etp.proxyId");
    public static final AttributeKey<String> OS = AttributeKey.valueOf("etp.os");
    public static final AttributeKey<String> ARCH = AttributeKey.valueOf("etp.arch");

}
