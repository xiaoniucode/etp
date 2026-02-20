package com.xiaoniucode.etp.core.constant;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeKey;

/**
 * 通道相关常量
 * @author liuxin
 */
public class ChannelConstants {
    public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("etp.sessionId");
    public static final AttributeKey<String> CLIENT_ID = AttributeKey.valueOf("etp.client_id");
    public static final AttributeKey<String> TOKEN = AttributeKey.valueOf("etp.token");
    public static final AttributeKey<String> BASIC_AUTH_HEADER = AttributeKey.valueOf("etp.token");
    public static final AttributeKey<Boolean> CONNECTED = AttributeKey.valueOf("etp.connected");
    public static final AttributeKey<ProtocolType> PROTOCOL_TYPE = AttributeKey.valueOf("etp.protocol_type");
    public static final AttributeKey<String> VISIT_DOMAIN = AttributeKey.valueOf("etp.visitorDomain");
    public static final AttributeKey<ByteBuf> HTTP_FIRST_PACKET = AttributeKey.newInstance("cachedFirstPacket");
}
