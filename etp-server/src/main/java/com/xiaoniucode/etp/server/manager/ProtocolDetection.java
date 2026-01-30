package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.codec.ProtocolType;
import io.netty.channel.Channel;

public class ProtocolDetection {
    public static ProtocolType getProtocolType(Channel visitor) {
        return visitor.attr(EtpConstants.PROTOCOL_TYPE).get();
    }

    public static boolean isHttp(Channel visitor) {
        ProtocolType protocolType = getProtocolType(visitor);
        return protocolType != null && ProtocolType.isHttp(protocolType);
    }

//    public static boolean isHttps(Channel visitor) {
//
//    }
//
//    public static boolean isHttpOrHttps(Channel visitor) {
//
//    }

    public static boolean isTcp(Channel visitor) {
        ProtocolType protocolType = getProtocolType(visitor);
        return protocolType == null || ProtocolType.isTcp(protocolType);
    }
}
