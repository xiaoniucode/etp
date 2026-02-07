package com.xiaoniucode.etp.core.enums;

/**
 * 代理状态枚举
 */
public enum ProxyStatus {
    CLOSED(0, "关闭"),
    OPEN(1, "开启");

    private final Integer code;
    private final String description;

    ProxyStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ProxyStatus fromStatus(Integer code) {
        for (ProxyStatus proxyStatus : values()) {
            if (proxyStatus.getCode().equals(code)) {
                return proxyStatus;
            }
        }
        throw new IllegalArgumentException("Unknown proxy code: " + code);
    }
}
