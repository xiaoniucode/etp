package cn.xilio.etp.server.web.digest;

/**
 * @author liuxin
 */
public final class DigestUtil {
    private static final String globalSalt = "XILIO-ETP-2025!@#";

    private DigestUtil() {
    }
    public static String encode(String password, String salt) {
        return MD5.of(password, globalSalt + salt);
    }
}
