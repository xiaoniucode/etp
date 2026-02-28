package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 流打开成功处理
 * 需要检查streamId
 */
@Component
public class StreamOpenResponseAction extends StreamBaseAction {
    private final Logger logger= LoggerFactory.getLogger(StreamContext.class);
    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        logger.debug("流打开成功");
        Channel tunnel = context.getTunnel();
        int streamId = context.getStreamId();

        Channel visitor = context.getVisitor();


//
//        visitor.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
//            @Override
//            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
//                TMSPFrame frame = new TMSPFrame(streamId, TMSP.MSG_STREAM_DATA, msg);
//                tunnel.writeAndFlush(frame);
//            }
//        });
        visitor.config().setOption(ChannelOption.AUTO_READ, true);
        if (context.getCurrentProtocol().isHttp()){
            context.relayHttpFirstPackage();
        }
    }
}
