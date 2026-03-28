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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TunnelEntry {
    private String tunnelId;
    private boolean active=false;
    private boolean encrypt;
    private Channel channel;
    private NettyBatchWriteQueue writeQueue;

    public TunnelEntry(String tunnelId,boolean encrypt, Channel channel, NettyBatchWriteQueue writeQueue) {
        this.tunnelId=tunnelId;
        this.channel = channel;
        this.encrypt=encrypt;
        this.writeQueue=writeQueue;
    }
}
