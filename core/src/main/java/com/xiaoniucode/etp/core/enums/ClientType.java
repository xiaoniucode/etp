package com.xiaoniucode.etp.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ClientType {
    /**
     * 临时客户端，生命周期同客户端连接session
     */
    WEB_SESSION(0, "web_session", true),
    /**
     * 二进制持久化设备
     */
    BINARY_DEVICE(1, "binary_device", false);
    private final Integer code;
    private final String description;
    private final boolean temporary;

    public static ClientType fromCode(Integer code) {
        for (ClientType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public boolean isWebSession() {
        return this == WEB_SESSION;
    }

    public boolean isBinaryDevice() {
        return this == BINARY_DEVICE;
    }
}
