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
public class HourlySnapshot {
    private final LocalDateTime hour;
    private final long inboundBytes;
    private final long outboundBytes;
    private final long inboundMessages;
    private final long outboundMessages;

    public HourlySnapshot(LocalDateTime hour, long inboundBytes, long outboundBytes,
                          long inboundMessages, long outboundMessages) {
        this.hour = hour;
        this.inboundBytes = inboundBytes;
        this.outboundBytes = outboundBytes;
        this.inboundMessages = inboundMessages;
        this.outboundMessages = outboundMessages;
    }
}