package com.xiaoniucode.etp.server.web.common;

/**
 * @author liuxin
 */
public final class DigestUtil {
    private static final String GLOBAL_SALT = "XILIO-ETP-2025!@#";

    private DigestUtil() {
    }
    public static String encode(String password, String salt) {
        return MD5.of(password, GLOBAL_SALT + salt);
    }
}
