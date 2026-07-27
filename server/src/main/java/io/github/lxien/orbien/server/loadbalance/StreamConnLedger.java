package io.github.lxien.orbien.server.loadbalance;

import java.util.concurrent.ConcurrentHashMap;

public final class StreamConnLedger {
    private final ConcurrentHashMap<Integer, String> streamKey = new ConcurrentHashMap<>();

    public boolean bindIfAbsent(int streamId, String key) {
        return streamKey.putIfAbsent(streamId, key) == null;
    }

    public String unbind(int streamId) {
        return streamKey.remove(streamId);
    }

    public boolean isBound(int streamId) {
        return streamKey.containsKey(streamId);
    }
}