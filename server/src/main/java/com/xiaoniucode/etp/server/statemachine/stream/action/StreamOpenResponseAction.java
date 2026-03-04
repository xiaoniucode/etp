package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.server.transport.DirectBridgeFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 流打开成功处理
 */
@Component
public class StreamOpenResponseAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(StreamOpenResponseAction.class);
    @Autowired
    private TunnelManager tunnelManager;

    @Override
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        Channel visitor = context.getVisitor();
        String tunnelId = context.getVariableAs("tunnelId", String.class);
        Integer connectionId = context.getVariableAs("connectionId", Integer.class);
        Optional<TunnelContext> tc = tunnelManager.getTunnel(context.isMux(), tunnelId);
        if (tc.isPresent()) {
            TunnelContext tunnelContext = tc.get();
            Channel tunnel = tunnelContext.getTunnel();
            context.setTunnel(tunnel);
            if (!context.getProxyConfig().isMuxTunnel()) {
                StreamManager visitorManager = context.getVisitorManager();
                //删除自定义协议
                visitor.pipeline().remove(NettyConstants.TCP_VISITOR_HANDLER);
                tunnel.pipeline().remove(NettyConstants.TMSP_CODEC);
                tunnel.pipeline().remove(NettyConstants.CONTROL_FRAME_HANDLER);
                tunnel.pipeline().remove(NettyConstants.IDLE_CHECK_HANDLER);
                //隧道桥接
                DirectBridgeFactory.bridge(visitorManager, visitor, tunnel, context.getCurrentProtocol());
                logger.debug("独立隧道建立成功: {}", context.getTarget());
            } else {
                logger.debug("共享隧道建立成功: {}", context.getTarget());
            }
            addCompressOrEncryptIfNecessary(context);
            //开启访问流可读
            visitor.config().setOption(ChannelOption.AUTO_READ, true);
            if (context.getCurrentProtocol().isHttp()) {
                context.relayHttpFirstPackage();
            }
        } else {
            context.fireEvent(StreamEvent.STREAM_OPEN_FAILURE);
        }
    }

    private void addCompressOrEncryptIfNecessary(StreamContext context) {
        Channel tunnel = context.getTunnel();
        ChannelPipeline pipeline = tunnel.pipeline();
        if (context.isCompress()) {
            pipeline.addLast(new SnappyEncoder());
            pipeline.addLast(new SnappyDecoder());

        }
    }
}
