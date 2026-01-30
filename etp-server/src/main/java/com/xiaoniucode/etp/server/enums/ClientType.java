package com.xiaoniucode.etp.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ClientType {
    /**
     * 临时客户端，生命周期同客户端连接session
     */
    WEB_SESSION("web_session", "临时客户端", true),
    /**
     * 二进制持久化设备
     */
    BINARY_DEVICE("binary_device", "二进制设备客户端", false);
    private final String code;
    private final String description;
    private final boolean temporary;

    public static ClientType fromCode(String code) {
        for (ClientType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
