package com.xiaoniucode.etp.server.web;

import java.util.Random;

/**
 * 验证码code生成工具类
 *
 * @author liuxin
 */
public class CaptchaGenerator {
    private static final int CODE_LENGTH = 4;
    /**
     * 字符集（排除易混淆字符）
     */
    private static final String CHAR_SET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Random random = new Random();

    public String generateCaptcha() {
        return generateRandomCode();
    }

    /**
     * 生成随机验证码字符串
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHAR_SET.length());
            sb.append(CHAR_SET.charAt(index));
        }
        return sb.toString();
    }
}
