package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StreamCloseAction extends StreamBaseAction{
    private final Logger logger= LoggerFactory.getLogger(StreamCloseAction.class);

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();

        Channel visitor = context.getVisitor();
        Channel control = context.getControl();
        visitor.close();
        TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_CLOSE);
        control.writeAndFlush(frame);
        context.getVisitorManager().removeStreamContext(streamId);
        logger.debug("关闭隧道 streamId={} host={} port={}",streamId,context.getCurrentTarget().getHost(),context.getCurrentTarget().getPort());
    }
}
