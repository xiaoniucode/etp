package com.xiaoniucode.etp.server.generator;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生成系统唯一ID
 *
 * @author liuxin
 */
public final class GlobalIdGenerator {


    private GlobalIdGenerator() {
    }

    public static String uuid32() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString();
    }
}
