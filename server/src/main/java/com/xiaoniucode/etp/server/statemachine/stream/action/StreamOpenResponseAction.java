package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.VisitorManager;
import com.xiaoniucode.etp.server.transport.DirectBridgeFactory;
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
    private final Logger logger = LoggerFactory.getLogger(StreamOpenResponseAction.class);

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        Channel visitor = context.getVisitor();
        if (!context.getProxyConfig().isMuxTunnel()) {
            VisitorManager visitorManager = context.getVisitorManager();
            Channel tunnel = context.getTunnel();
            //删除自定义协议
            visitor.pipeline().remove(NettyConstants.TCP_VISITOR_HANDLER);
            tunnel.pipeline().remove(NettyConstants.TMSP_CODEC);
            tunnel.pipeline().remove(NettyConstants.CONTROL_FRAME_HANDLER);
            tunnel.pipeline().remove(NettyConstants.IDLE_CHECK_HANDLER);

            //隧道桥接
            DirectBridgeFactory.bridge(visitorManager, visitor, tunnel, context.getCurrentProtocol());
            logger.debug("独立隧道建立成功: {}",context.getTarget());
        }else {
            logger.debug("共享隧道建立成功: {}",context.getTarget());
        }
        //开启访问流可读
        visitor.config().setOption(ChannelOption.AUTO_READ, true);
        if (context.getCurrentProtocol().isHttp()) {
            context.relayHttpFirstPackage();
        }
    }
}
