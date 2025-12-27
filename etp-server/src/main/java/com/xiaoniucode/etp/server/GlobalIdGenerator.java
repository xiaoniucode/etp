package com.xiaoniucode.etp.server;

import java.util.concurrent.atomic.AtomicInteger;

public final class GlobalIdGenerator {

    private static final AtomicInteger ID = new AtomicInteger(0);

    private GlobalIdGenerator() {}

    /**
     * 返回全局自增ID
     */
    public static int nextId() {
        return ID.incrementAndGet();
    }
}
