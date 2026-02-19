package com.xiaoniucode.etp.core.enums;

/**
 * IP 访问控制模式
 */
public enum AccessControlMode {
    /**
     * 白名单模式：只允许指定 IP 访问
     */
    ALLOW(1, "白名单"),

    /**
     * 黑名单模式：拒绝指定 IP 访问，允许其他 IP 访问
     */
    DENY(0, "黑名单");

    private final Integer code;
    private final String description;

    AccessControlMode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码值获取对应的枚举实例
     *
     * @param code 代码值（如 1、0）
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 当代码值不存在对应的枚举实例时
     */
    public static AccessControlMode fromCode(Integer code) {
        for (AccessControlMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("未知访问控制模式: " + code);
    }

    /**
     * 根据字符串值获取对应的枚举实例
     *
     * @param value 字符串值（如 "allow"、"deny"）
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 当字符串值不存在对应的枚举实例时
     */
    public static AccessControlMode fromValue(String value) {
        for (AccessControlMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("无效的访问控制模式: " + value);
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAllowMode() {
        return this == ALLOW;
    }
    public boolean isDenyMode() {
        return this == DENY;
    }
}