package com.xiaoniucode.etp.client.common;

import java.util.UUID;

public class UUIDGenerator {

    private UUIDGenerator() {

    }

    /**
     * 生成32位的UUID
     * @return 32位的UUID字符串
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
