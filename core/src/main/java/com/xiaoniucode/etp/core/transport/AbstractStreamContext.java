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

import com.xiaoniucode.etp.core.statemachine.context.ProcessContextImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public abstract class AbstractStreamContext extends ProcessContextImpl {
    protected int streamId;
    protected TunnelEntry tunnelEntry;
    protected boolean compress;
    protected boolean encrypt;
    protected boolean multiplex;
    protected AbstractAgentContext agentContext;
    protected TunnelBridge tunnelBridge;

    public void forwardToRemote(ByteBuf payload) {
        if (tunnelBridge == null) {
            return;
        }
        tunnelBridge.forwardToRemote(payload);
    }

    public void forwardToLocal(ByteBuf payload) {
        if (tunnelBridge == null) {
            return;
        }
        tunnelBridge.forwardToLocal(payload);
    }

    public boolean isChannelClosed(Channel channel) {
        return channel == null ||! channel.isActive() && !channel.isWritable() ;
    }
}
