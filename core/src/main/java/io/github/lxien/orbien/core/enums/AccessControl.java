package io.github.lxien.orbien.core.enums;

import lombok.Getter;

/**
 * IP 访问控制模式
 *
 * @author lxien
 */
@Getter
public enum AccessControl {
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

    AccessControl(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AccessControl fromCode(Integer code) {
        for (AccessControl mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("未知访问控制模式: " + code);
    }

    public static AccessControl fromValue(String value) {
        for (AccessControl mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("无效的访问控制模式: " + value);
    }

    public boolean isAllowMode() {
        return this == ALLOW;
    }

    public boolean isDenyMode() {
        return this == DENY;
    }
}