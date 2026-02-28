package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.client.statemachine.stream.StreamState;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamCloseAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamCloseAction.class);

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        int streamId = context.getStreamId();

        Channel server = context.getServer();
        ChannelUtils.closeOnFlush(server);
        StreamManager.removeStreamContext(streamId);
        //todo 判断请求来自哪里 control.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_CLOSE));
        logger.debug("隧道关闭 - localIp={} localPort={}", context.getLocalIp(),context.getLocalPort());
    }
}
