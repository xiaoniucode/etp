/*
 *    Copyright 2026 lxien
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

package io.github.lxien.orbien.core.transport;

import io.netty.channel.Channel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 背压流注册表
 *
 * @author lxien
 */
public class PausedStreamRegistry {

    private final ConcurrentMap<Channel, ConcurrentMap<Integer, Boolean>> pausedStreamByChannel = new ConcurrentHashMap<>();

    private final ConcurrentMap<Integer, Set<Channel>> channelsByStreamId = new ConcurrentHashMap<>();

    /**
     * 记录指定隧道上暂停的流ID
     *
     * @param tunnel   隧道通道
     * @param streamId 流标识符
     */
    public void addPausedStreamId(Channel tunnel, int streamId) {
        pausedStreamByChannel.computeIfAbsent(tunnel, k -> new ConcurrentHashMap<>())
                .putIfAbsent(streamId, Boolean.TRUE);

        channelsByStreamId.computeIfAbsent(streamId,
                        id -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(tunnel);
    }

    /**
     * 获取指定隧道上所有暂停的流ID集合
     *
     * @param tunnel 隧道通道
     * @return 暂停的流ID集合，若没有则返回空集合
     */
    public Set<Integer> getPausedStreamIds(Channel tunnel) {
        ConcurrentMap<Integer, Boolean> streams = pausedStreamByChannel.get(tunnel);
        if (streams == null || streams.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(streams.keySet()));
    }

    /**
     * 移除指定隧道上的某个暂停流记录
     *
     * @param tunnel   隧道通道
     * @param streamId 流标识符
     */
    public void removePausedStream(Channel tunnel, int streamId) {
        ConcurrentMap<Integer, Boolean> streams = pausedStreamByChannel.get(tunnel);
        if (streams != null) {
            streams.remove(streamId);
            if (streams.isEmpty()) {
                pausedStreamByChannel.remove(tunnel, streams);
            }
        }

        Set<Channel> channels = channelsByStreamId.get(streamId);
        if (channels != null) {
            channels.remove(tunnel);
            if (channels.isEmpty()) {
                channelsByStreamId.remove(streamId, channels);
            }
        }
    }

    /**
     * 清理指定流ID的所有暂停记录
     *
     * @param streamId 流标识符
     */
    public void clear(Integer streamId) {
        Set<Channel> channels = channelsByStreamId.remove(streamId);
        if (channels != null) {
            for (Channel channel : channels) {
                ConcurrentMap<Integer, Boolean> streams = pausedStreamByChannel.get(channel);
                if (streams != null) {
                    streams.remove(streamId);
                    if (streams.isEmpty()) {
                        pausedStreamByChannel.remove(channel, streams);
                    }
                }
            }
        }
    }

    /**
     * 清理指定隧道的所有暂停流记录
     *
     * @param tunnel 隧道通道
     */
    public void removeByChannel(Channel tunnel) {
        ConcurrentMap<Integer, Boolean> streams = pausedStreamByChannel.remove(tunnel);
        if (streams != null) {
            for (Integer streamId : streams.keySet()) {
                Set<Channel> channels = channelsByStreamId.get(streamId);
                if (channels != null) {
                    channels.remove(tunnel);
                    if (channels.isEmpty()) {
                        channelsByStreamId.remove(streamId, channels);
                    }
                }
            }
        }
    }

}
