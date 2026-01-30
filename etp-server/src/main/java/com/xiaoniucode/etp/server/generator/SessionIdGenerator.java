package com.xiaoniucode.etp.server.generator;

import java.util.concurrent.atomic.AtomicLong;

public final class SessionIdGenerator {
    private static final AtomicLong AGENT_SESSION_SEQ = new AtomicLong(0);
    private static final AtomicLong VISITOR_SESSION_SEQ = new AtomicLong(0);

    /**
     * Agent会话ID - 格式: SESS_AGENT_时间戳_序列号
     */
    public synchronized String nextAgentSessionId() {
        long seq = AGENT_SESSION_SEQ.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        String timePart = Long.toString(timestamp % 100_000_000L, 36);
        return String.format("SESS_AGENT_%s_%04d", timePart.toUpperCase(), seq % 10000);
    }

    /**
     * Visitor会话ID - 格式: SESS_VISITOR_时间戳_序列号
     */
    public synchronized String nextVisitorSessionId() {
        long seq = VISITOR_SESSION_SEQ.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        String timePart = Long.toString(timestamp % 100_000_000L, 36);
        return String.format("SESS_VISITOR_%s_%04d", timePart.toUpperCase(), seq % 10000);
    }
}