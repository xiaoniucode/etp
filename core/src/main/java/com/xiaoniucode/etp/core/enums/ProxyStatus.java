package com.xiaoniucode.etp.core.enums;

/**
 * 代理状态枚举
 */
public enum ProxyStatus {
    CLOSED(0, "关闭"),
    OPEN(1, "开启"),
    DELETED(2, "已删除");

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
        throw new IllegalArgumentException("未知代理状态: " + code);
    }

    /**
     * 状态切换 关闭<-->开启
     *
     * @return 切换后的状态
     */
    public ProxyStatus toggle() {
        if (this == OPEN) {
            return CLOSED;
        } else if (this == CLOSED) {
            return OPEN;
        }
        throw new IllegalStateException("只有开启或关闭状态才能切换，当前状态: " + this);
    }

    public boolean isOpen() {
        return this == OPEN;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
