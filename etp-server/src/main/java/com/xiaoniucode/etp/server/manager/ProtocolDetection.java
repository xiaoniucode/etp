package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.constant.ChannelConstants;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import io.netty.channel.Channel;

public class ProtocolDetection {
    public static ProtocolType getProtocolType(Channel visitor) {
        return visitor.attr(ChannelConstants.PROTOCOL_TYPE).get();
    }

    public static boolean isHttp(Channel visitor) {
        ProtocolType protocolType = getProtocolType(visitor);
        return protocolType != null && ProtocolType.isHttp(protocolType);
    }

    public static boolean isTcp(Channel visitor) {
        ProtocolType protocolType = getProtocolType(visitor);
        return protocolType == null || ProtocolType.isTcp(protocolType);
    }
}
