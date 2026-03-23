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

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

public class CompressTunnelBridgeDecorator extends AbstractTunnelBridgeDecorator {
    private final AbstractStreamContext streamContext;
    public CompressTunnelBridgeDecorator(TunnelBridge delegate, AbstractStreamContext streamContext) {
        super(delegate);
        this.streamContext = streamContext;
    }

    @Override
    public void open() {
        //Lz4FrameDecoder lz4FrameDecoder = new Lz4FrameDecoder();
       // Lz4FrameEncoder lz4FrameEncoder = new Lz4FrameEncoder();
        Channel tunnel = streamContext.getTunnelEntry().getChannel();
        if (tunnel == null) {
            delegate.open();
            return;
        }
        ChannelPipeline pipeline = tunnel.pipeline();
        boolean compress = streamContext.isCompress();

        if (compress) {
            if (pipeline.get(NettyConstants.SNAPPY_DECODER) == null) {
                pipeline.addBefore(NettyConstants.TMSP_CODEC,NettyConstants.SNAPPY_DECODER, new SnappyDecoder());
            }
            if (pipeline.get(NettyConstants.SNAPPY_ENCODER) == null) {
                pipeline.addAfter(NettyConstants.CONTROL_FRAME_HANDLER,NettyConstants.SNAPPY_ENCODER, new SnappyEncoder());
            }

        } else {
            if (pipeline.get(NettyConstants.SNAPPY_ENCODER) != null) {
                pipeline.remove(NettyConstants.SNAPPY_ENCODER);
            }
            if (pipeline.get(NettyConstants.SNAPPY_DECODER) != null) {
                pipeline.remove(NettyConstants.SNAPPY_DECODER);
            }
        }

        delegate.open();
    }

    @Override
    public void forwardToLocal(ByteBuf payload) {
        super.forwardToLocal(payload);
    }

    @Override
    public void forwardToRemote(ByteBuf payload) {
        super.forwardToRemote(payload);
    }
}