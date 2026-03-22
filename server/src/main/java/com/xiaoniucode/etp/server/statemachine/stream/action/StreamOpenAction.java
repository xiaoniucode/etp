package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.codec.NewStreamCodec;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.server.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class StreamOpenAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamOpenAction.class);

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();
        AgentContext agentContext =(AgentContext) context.getAgentContext();
        Channel control = agentContext.getControl();
        ProxyConfig config = context.getProxyConfig();
        if (!control.isActive()) {
            logger.warn("当前控制通道已关闭: proxyName={}", config.getName());
            context.fireEvent(StreamEvent.STREAM_CLOSE);
            return;
        }

        Target target = context.getCurrentTarget();

        ByteBuf buffer = control.alloc().buffer();
        NewStreamCodec.encode(buffer, target.getHost(), target.getPort());

        TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_OPEN, buffer);

        frame.setMuxTunnel(config.isMuxTunnel());
        frame.setCompressed(context.isCompress());
        frame.setEncrypted(config.isEncrypt());

        control.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                buffer.release();
                context.fireEvent(StreamEvent.STREAM_CLOSE);
            } else {
                control.eventLoop().schedule(() -> {
                    if (context.getState() == StreamState.OPENING) {
                        logger.warn("打开流超时: stream={}", streamId);
                        context.fireEvent(StreamEvent.STREAM_CLOSE);
                    }
                }, 10, TimeUnit.SECONDS); // 如果10秒状态未改变，超时处理
                logger.debug("Stream open success: proxyName: {} - target: {}", config.getName(), target);
            }
        });
    }
}
