package cn.xilio.etp.server.web.framework;

/**
 * @author liuxin
 */
public class BizException extends RuntimeException {
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
        if (null == value) {
            throw new BizException(msg);
        }
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
