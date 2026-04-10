/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.common.utils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
/**
 * MD5 工具类
 * @author liuxin
 */
public final class MD5 {
    private MD5() {
    }
    private static final MessageDigest MD;
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    static {
        try {
            MD = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }
    /**
     * 带盐 MD5
     * 用户ID + 固定全局盐（防止彩虹表）
     */
    public static String of(String password, String salt) {
        if (password == null || password.isEmpty()) {
            password = "";
        }
        if (salt == null) {
            salt = "";
        }
        String raw = salt + password + salt;
        return of(raw.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 只在后面加盐
     */
    public static String ofWithSalt(String password, String salt) {
        if (password == null) {
            password = "";
        }
        if (salt == null) {
            salt = "";
        }
        return of((password + salt).getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 无盐
     */
    public static String of(String text) {
        if (text == null || text.isEmpty()) {
            return "d41d8cd98f00b204e9800998ecf8427e";
        }
        return of(text.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * byte[] → 32位小写 hex
     */
    private static String of(byte[] data) {
        MessageDigest digest;
        try {
            digest = (MessageDigest) MD.clone();
        } catch (Exception e) {
            digest = MD;
        }
        byte[] hash = digest.digest(data);
        char[] result = new char[32];
        for (int i = 0; i < 16; i++) {
            int b = hash[i] & 0xFF;
            result[i << 1] = HEX[b >>> 4];
            result[(i << 1) + 1] = HEX[b & 0x0F];
        }
        return new String(result);
    }
}
