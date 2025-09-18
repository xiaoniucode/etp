package cn.xilio.etp.core.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 支持代理的协议类型
 */
public enum ProtocolType {
    TCP;
    /**
     * 用于缓存协议类型，提高性能
     */
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
}
