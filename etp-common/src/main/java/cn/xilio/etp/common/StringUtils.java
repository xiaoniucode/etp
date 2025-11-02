package cn.xilio.etp.common;

/**
 * 字符串相关工具函数
 *
 * @author liuxin
 */
public abstract class StringUtils {
    public static boolean hasLength(CharSequence str) {
        return (str != null && !str.isEmpty());
    }

    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    public static boolean hasText(CharSequence str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
