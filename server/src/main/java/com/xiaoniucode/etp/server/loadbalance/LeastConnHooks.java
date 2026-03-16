package com.xiaoniucode.etp.server.loadbalance;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;

import java.util.Objects;

public final class LeastConnHooks {
    private final LeastConnectionCounter counter;
    private final StreamConnLedger ledger;

    public LeastConnHooks(LeastConnectionCounter counter, StreamConnLedger ledger) {
        this.counter = Objects.requireNonNull(counter);
        this.ledger = Objects.requireNonNull(ledger);
    }

    public void onStreamOpened(StreamContext ctx) {
        ProxyConfig pc = ctx.getProxyConfig();
        Target t = ctx.getCurrentTarget();
        if (pc == null || pc.getProxyId() == null || t == null ||
                t.getHost() == null || t.getPort() == null) {
            return;
        }
        int streamId = ctx.getStreamId();
        String key = pc.getProxyId() + ":" + t.getHost() + ":" + t.getPort();
        if (ledger.bindIfAbsent(streamId, key)) {
            counter.inc(key);
        }
    }
    public void onStreamClosed(StreamContext ctx) {
        int streamId = ctx.getStreamId();
        String key = ledger.unbind(streamId);
        counter.dec(key);
    }
}