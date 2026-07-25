package io.github.lxien.orbien.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AuthStatusCode {

    SUCCESS(0, "认证成功"),
    FAILURE(1, "认证失败"),
    INVALID_TOKEN(100, "无效令牌"),
    AGENT_NOT_FOUND(101, "设备ID不存在");

    private final int code;
    private final String description;

    public static AuthStatusCode fromCode(int code) {
        for (AuthStatusCode statusCode : values()) {
            if (statusCode.code == code) {
                return statusCode;
            }
        }
        return null;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isRecoverable() {
        return this == AGENT_NOT_FOUND;
    }
}
