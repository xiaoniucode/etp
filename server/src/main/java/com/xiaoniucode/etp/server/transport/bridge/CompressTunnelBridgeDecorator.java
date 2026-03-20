package com.xiaoniucode.etp.server.transport.bridge;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

public class CompressTunnelBridgeDecorator extends AbstractTunnelBridgeDecorator {
    public CompressTunnelBridgeDecorator(TunnelBridge delegate, StreamContext streamContext) {
        super(delegate, streamContext);
    }

    @Override
    public void open() {
        Channel tunnel = streamContext.getTunnel();
        if (tunnel == null) {
            delegate.open();
            return;
        }
        ChannelPipeline pipeline = tunnel.pipeline();
        boolean compress = streamContext.isCompress();

        if (compress) {
            if (pipeline.get(NettyConstants.SNAPPY_ENCODER) == null) {
                pipeline.addLast(NettyConstants.SNAPPY_ENCODER, new SnappyEncoder());
            }
            if (pipeline.get(NettyConstants.SNAPPY_DECODER) == null) {
                pipeline.addLast(NettyConstants.SNAPPY_DECODER, new SnappyDecoder());
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
}