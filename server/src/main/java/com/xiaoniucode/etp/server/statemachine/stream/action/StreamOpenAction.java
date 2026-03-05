package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.codec.NewStreamCodec;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.springframework.stereotype.Component;

@Component
public class StreamOpenAction extends StreamBaseAction {
    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();
        Channel control = context.getControl();
        ProxyConfig config = context.getProxyConfig();
        LoadBalancer loadBalancer = context.getLoadBalancer();
        Target target;
        if (config.isLoadBalanceNeeded() && loadBalancer != null) {
            target = loadBalancer.select(config.getTargets(), config.getProxyId());
        } else {
            target = config.getSingleTarget();

        }
        context.setTarget(target);
        ByteBuf buffer = control.alloc().buffer();
        NewStreamCodec.encode(buffer, target.getHost(), target.getPort());
        TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_OPEN, buffer);
        boolean muxTunnel = config.isMuxTunnel();
        frame.setMuxTunnel(muxTunnel);
        frame.setCompressed(false);
        frame.setEncrypted(config.isEncryptEnabled());
        control.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                context.fireEvent(StreamEvent.STREAM_CLOSE);
            }
        });
    }
}
