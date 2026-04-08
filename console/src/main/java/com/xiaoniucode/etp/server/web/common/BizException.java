package com.xiaoniucode.etp.server.web.common;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * @author liuxin
 */
public class BizException extends RuntimeException {
    @Getter
    private final int code;
    private final String message;
    public BizException(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public BizException(String message) {
        this.code = 1;
        this.message = message;
    }
    public static void throwIfNull(Object value, String msg) {
        if (value == null || !StringUtils.hasText(value.toString())) {
            throw new BizException(msg);
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
