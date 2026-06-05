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

package com.xiaoniucode.etp.server.metrics;

import lombok.Data;

/**
 * 速率滑动窗口中的累计值快照。
 *
 * <p>由 {@link ProxyMetrics#updateRate()} 周期性写入速率环，字段值为进程启动后的累计量，
 * 速率通过相邻快照的差值计算。
 */
@Data
public class RateSnapshot {

    private final long readBytes;
    private final long writeBytes;
    private final long readMessages;
    private final long writeMessages;
    private final long timestamp;

    /**
     * @param timestamp     采样时刻，毫秒时间戳
     * @param readBytes     累计入站字节数
     * @param writeBytes    累计出站字节数
     * @param readMessages  累计入站消息数
     * @param writeMessages 累计出站消息数
     */
    public RateSnapshot(long timestamp, long readBytes, long writeBytes, long readMessages, long writeMessages) {
        this.timestamp = timestamp;
        this.readBytes = readBytes;
        this.writeBytes = writeBytes;
        this.readMessages = readMessages;
        this.writeMessages = writeMessages;
    }
}
