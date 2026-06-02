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

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HourlyTraffic {
    private final LocalDateTime hour;
    private final long readBytes;
    private final long writeBytes;
    private final long readMessages;
    private final long writeMessages;

    public HourlyTraffic(HourlySnapshot snap) {
        this.hour = snap.getHour();
        this.readBytes = snap.getReadBytes();
        this.writeBytes = snap.getWriteBytes();
        this.readMessages = snap.getReadMessages();
        this.writeMessages = snap.getWriteMessages();
    }

    public HourlyTraffic(LocalDateTime hour, long readBytes, long writeBytes,
                         long readMessages, long writeMessages) {
        this.hour = hour;
        this.readBytes = readBytes;
        this.writeBytes = writeBytes;
        this.readMessages = readMessages;
        this.writeMessages = writeMessages;
    }
}