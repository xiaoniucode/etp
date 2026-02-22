package com.xiaoniucode.etp.server.generator;

import java.util.concurrent.atomic.AtomicLong;

public final class SessionIdGenerator {
    private static final AtomicLong SESSION_SEQ = new AtomicLong(0);

    public String nextSessionId() {
        long seq = SESSION_SEQ.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        return timestamp + "-" + seq;
    }
}