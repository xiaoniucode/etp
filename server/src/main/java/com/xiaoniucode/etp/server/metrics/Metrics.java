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

import java.time.LocalDateTime;

public class Metrics {
    private String proxyId;
    private int activeChannels;
    private long readBytes;
    private long writeBytes;
    private long readMessages;
    private long writeMessages;
    private double readRate;
    private double writeRate;
    private LocalDateTime lastActiveTime;

    public String getProxyId() { return proxyId; }
    public void setProxyId(String proxyId) { this.proxyId = proxyId; }

    public int getActiveChannels() { return activeChannels; }
    public void setActiveChannels(int activeChannels) { this.activeChannels = activeChannels; }

    public long getReadBytes() { return readBytes; }
    public void setReadBytes(long readBytes) { this.readBytes = readBytes; }

    public long getWriteBytes() { return writeBytes; }
    public void setWriteBytes(long writeBytes) { this.writeBytes = writeBytes; }

    public long getReadMessages() { return readMessages; }
    public void setReadMessages(long readMessages) { this.readMessages = readMessages; }

    public long getWriteMessages() { return writeMessages; }
    public void setWriteMessages(long writeMessages) { this.writeMessages = writeMessages; }

    public double getReadRate() { return readRate; }
    public void setReadRate(double readRate) { this.readRate = readRate; }

    public double getWriteRate() { return writeRate; }
    public void setWriteRate(double writeRate) { this.writeRate = writeRate; }

    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }
}