package com.xiaoniucode.etp.core.codec;

import com.xiaoniucode.etp.common.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 支持的协议类型
 *
 * @author liuxin
 */
public enum ProtocolType {
    TCP, HTTP;
    private static final Map<String, ProtocolType> NAME_MAP;

    static {
        Map<String, ProtocolType> map = new HashMap<>();
        for (ProtocolType protocol : values()) {
            map.put(protocol.name().toLowerCase(), protocol);
        }
        NAME_MAP = Collections.unmodifiableMap(map);
    }

    public static ProtocolType getType(String type) {
        ProtocolType protocol = NAME_MAP.get(type.toLowerCase());
        if (protocol == null) {
            throw new IllegalArgumentException("无效协议类型,暂不支持： " + type);
        }
        return protocol;
    }

    public static boolean isTcp(ProtocolType protocolType) {
        return protocolType == TCP;
    }

    public static boolean isHttp(ProtocolType protocolType) {
        return protocolType == HTTP;
    }

    public static boolean isHttp(String protocol) {
        return StringUtils.hasText(protocol) && HTTP.name().equalsIgnoreCase(protocol);
    }

    public static boolean isTcp(String protocol) {
        return StringUtils.hasText(protocol) && TCP.name().equalsIgnoreCase(protocol);
    }
}
