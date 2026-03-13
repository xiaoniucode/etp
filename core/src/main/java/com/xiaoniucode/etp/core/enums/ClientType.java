package com.xiaoniucode.etp.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ClientType {
    /**
     * 会话级客户端
     */
    SESSION_CLINT(0, "Session client", true),
    /**
     * 二进制客户端
     */
    BINARY_DEVICE(1, "Binary device", false);
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
        return this == SESSION_CLINT;
    }

    public boolean isBinaryDevice() {
        return this == BINARY_DEVICE;
    }
}
