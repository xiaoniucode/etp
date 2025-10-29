package cn.xilio.etp.core;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Map;

/**
 * @author liuxin
 */
public class EtpConstants {
    // ====================== 会话隧道 ======================
    public static final AttributeKey<Long> SESSION_ID = AttributeKey.valueOf("etp.sessionId");
    public static final AttributeKey<String> SECRET_KEY = AttributeKey.valueOf("etp.secretKey");

    // ====================== 通道关联（双向绑定） ======================
    public static final AttributeKey<Channel> DATA_CHANNEL = AttributeKey.valueOf("etp.dataChannel");
    public static final AttributeKey<Channel> CONTROL_CHANNEL = AttributeKey.valueOf("etp.controlChannel");
    public static final AttributeKey<Channel> REAL_SERVER_CHANNEL = AttributeKey.valueOf("etp.realServerChannel");
    public static final AttributeKey<Channel> CLIENT_CHANNEL = AttributeKey.valueOf("etp.clientChannel");

    // ====================== 其他 ======================
    public static final AttributeKey<Map<Long, Channel>> CLIENT_CHANNELS = AttributeKey.newInstance("etp.clientChannels");
    public static final AttributeKey<List<Integer>> CHANNEL_PORT = AttributeKey.valueOf("etp.channelPort");

}
