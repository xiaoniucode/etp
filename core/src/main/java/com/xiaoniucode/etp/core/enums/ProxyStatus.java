package com.xiaoniucode.etp.core.enums;

import lombok.Getter;

@Getter
public enum ProxyStatus {
    CLOSED(0, "关闭"),
    OPEN(1, "开启");

    private final Integer code;
    private final String description;

    ProxyStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProxyStatus fromStatus(Integer code) {
        for (ProxyStatus proxyStatus : values()) {
            if (proxyStatus.getCode().equals(code)) {
                return proxyStatus;
            }
        }
        throw new IllegalArgumentException("未知代理状态: " + code);
    }

    public boolean isOpen() {
        return this == OPEN;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }
}
