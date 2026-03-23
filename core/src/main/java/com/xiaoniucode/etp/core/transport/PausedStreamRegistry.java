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

package com.xiaoniucode.etp.core.transport;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 背压，隧道不可写的时候，暂停对应隧道的流转发，记录暂停的流ID列表，等隧道可写时再恢复转发
 */
public class PausedStreamRegistry {
    private final ConcurrentHashMap<Channel, IntSet> pausedStream = new ConcurrentHashMap<>();

    public void addPausedStreamId(Channel tunnel, int streamId) {
        pausedStream.computeIfAbsent(tunnel, k -> new IntSet()).add(streamId);
    }

    public IntSet getPausedStreamIds(Channel tunnel) {
        return pausedStream.getOrDefault(tunnel, new IntSet());
    }

    public void removePausedStream(Channel tunnel, int streamId) {
        IntSet streamIds = pausedStream.get(tunnel);
        if (streamIds != null) {
            streamIds.remove(streamId);
            if (streamIds.isEmpty()) {
                pausedStream.remove(tunnel);
            }
        }
    }
}