package com.xiaoniucode.etp.client.statemachine.stream.action;

import com.xiaoniucode.etp.client.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.client.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.client.statemachine.stream.StreamState;

public class StreamCloseAction extends StreamBaseAction{
    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        //        int streamId = frame.getStreamId();
//
//        ServerStreamManager.getServerSession(streamId).ifPresent(serverStream -> {
//            Channel server = serverStream.getServer();
//            Channel tunnel = serverStream.getTunnel();
//            //归还连接到连接池
//            ConnectionPool.release(tunnel);
//            ChannelUtils.closeOnFlush(server);
//            logger.debug("隧道关闭 - [会话标识={}]", serverStream.getStreamId());
//        });
//
//        StreamContext stream = connCtx.getStream(streamId);
//        StreamFsm.transition(stream, StateEvent.RECV_STREAM_CLOSE);//如果是独立隧道，将连接放回连接池




//        Channel server = ctx.channel();
//        ServerStreamManager.removeServerSession(server).ifPresent(serverSession -> {
//            Channel control = serverSession.getAgentSession().getControl();
//            int streamId = serverSession.getStreamId();
//            Target target = serverSession.getTarget();
//            ChannelUtils.closeOnFlush(server);
//            //  control.writeAndFlush(new TMSPFrame(streamId, TMSP.MSG_CLOSE));
//            logger.debug("隧道关闭 - [目标地址={}，目标端口={}]", target.getHost(), target.getPort());
//        });
    }
}
