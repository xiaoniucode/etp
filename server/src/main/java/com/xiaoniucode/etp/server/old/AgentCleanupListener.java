//package com.xiaoniucode.etp.server.statemachine.listener.connection;
//
//import com.xiaoniucode.etp.core.statemachine.ConnStateListener;
//import com.xiaoniucode.etp.core.statemachine.ConnectionContext;
//import com.xiaoniucode.etp.core.statemachine.ConnectionState;
//import com.xiaoniucode.etp.core.statemachine.StateEvent;
//import com.xiaoniucode.etp.server.manager.news.AgentManager;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class AgentCleanupListener implements ConnStateListener {
//    @Autowired
//    private AgentManager agentManager;
//
//
//    @Override
//    public void onStateChanged(ConnectionContext ctx, ConnectionState oldState, ConnectionState newState, StateEvent trigger, Object payload) {
//
////
////        //连接断开
////        if (trigger == StateEvent.SEND_GOAWAY) {
////            agentSessionManager.disconnect(control);
////            Set<Integer> remotePorts = agentSessionManager.getAgentRemotePorts(control);
////            Set<String> domains = agentSessionManager.getAgentDomains(control);
////            visitorSessionManager.closeAllStreamsForAgent(control, remotePorts, domains);
////            ChannelUtils.closeOnFlush(control);
////            logger.debug("客户端连接资源释放完成");
////        }
////        if (trigger == StateEvent.RECV_STREAM_CLOSE) {
////            visitorSessionManager.closeStream(streamId, session -> {
////                        //减少最少连接负载均衡器连接计数
////                        LeastConnUtils.decrementConnection(session);
////                        ChannelUtils.closeOnFlush(session.getVisitor());
////                        logger.debug("visitor: {} 断开连接", session.getStreamId());
////                    }
////            );
////        }
////        if (trigger == StateEvent.SEND_PING_PONG) {
////            Channel control = ctx.channel();
////            //更新代理客户端最后心跳时间
////            agentSessionManager.updateHeartbeat(control);
////            control.writeAndFlush(new TMSPFrame(0, TMSP.MSG_PONG));
////        }
////        if (trigger == StateEvent.SEND_STREAM_RESET) {
////            visitorSessionManager.closeStream(streamId, session -> {
////                        //减少最少连接负载均衡器连接计数
////                        LeastConnUtils.decrementConnection(session);
////                        ChannelUtils.closeOnFlush(session.getVisitor());
////                        logger.debug("visitor: {} 断开连接", session.getStreamId());
////                    }
////            );
////        }
//    }
//}
