package com.xiaoniucode.etp.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agent 类型
 */
@AllArgsConstructor
@Getter
public enum AgentType {

    /**
     * 会话级 Agent（集成在 Spring Boot 等框架中，短生命周期、一次性使用）
     */
    SESSION(0, "Session Agent", true),

    /**
     * 二进制 Agent（独立运行的可执行程序，长期运行，支持配置持久化）
     */
    BINARY(1, "Binary Agent", false);

    private final Integer code;
    private final String description;
    private final boolean temporary;

    public static AgentType fromCode(Integer code) {
        for (AgentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public boolean isSession() {
        return this == SESSION;
    }

    public boolean isBinary() {
        return this == BINARY;
    }
}
