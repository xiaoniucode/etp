package com.xiaoniucode.etp.server.enums;

/**
 * 代理类型枚举
 */
public enum ProxyType {
    TCP("TCP", "TCP 代理"),
    HTTP("HTTP", "HTTP 代理"),
    HTTPS("HTTPS", "HTTPS 代理");

    private final String type;
    private final String description;

    ProxyType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public static ProxyType fromType(String type) {
        for (ProxyType proxyType : values()) {
            if (proxyType.getType().equals(type)) {
                return proxyType;
            }
        }
        throw new IllegalArgumentException("Unknown proxy type: " + type);
    }
}
