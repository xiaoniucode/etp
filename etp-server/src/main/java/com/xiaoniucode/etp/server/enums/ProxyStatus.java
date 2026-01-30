package com.xiaoniucode.etp.server.enums;

/**
 * 代理状态枚举
 */
public enum ProxyStatus {
    CLOSED(0, "关闭"),
    OPEN(1, "开启");

    private final Integer status;
    private final String description;

    ProxyStatus(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static ProxyStatus fromStatus(Integer status) {
        for (ProxyStatus proxyStatus : values()) {
            if (proxyStatus.getStatus().equals(status)) {
                return proxyStatus;
            }
        }
        throw new IllegalArgumentException("Unknown proxy status: " + status);
    }
}
