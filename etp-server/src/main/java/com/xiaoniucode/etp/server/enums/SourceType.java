package com.xiaoniucode.etp.server.enums;

/**
 * 注册类型枚举
 */
public enum SourceType {
    MANUAL(0, "手动注册"),
    AUTO(1, "自动注册");

    private final Integer type;
    private final String description;

    SourceType(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public static SourceType fromType(Integer type) {
        for (SourceType sourceType : values()) {
            if (sourceType.getType().equals(type)) {
                return sourceType;
            }
        }
        throw new IllegalArgumentException("Unknown source type: " + type);
    }
}
