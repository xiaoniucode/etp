package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
import com.xiaoniucode.etp.core.netty.NettyConstants;
import com.xiaoniucode.etp.core.netty.TlsHandlerCleanup;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelContext;
import com.xiaoniucode.etp.server.statemachine.tunnel.TunnelManager;
import com.xiaoniucode.etp.server.transport.DirectBridgeFactory;
import com.xiaoniucode.etp.server.transport.TlsContextHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
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
        String tunnelId = context.getVariableAs("tunnelId", String.class);
        Optional<TunnelContext> tc = tunnelManager.getTunnel(context.isMux(), tunnelId);
        if (tc.isPresent()) {
            Channel tunnel = tc.get().getTunnel();
            context.setTunnel(tunnel);
            //处理独立隧道，协议转换，隧道桥接
            handleDirectTunnel(context);
            if (context.isMux()) {
                logger.debug("共享隧道建立成功: {}", context.getTarget());
            }
            //如果是 HTTP协议需要发送首次建立建立的时候读取到的第一个包
            if (context.getCurrentProtocol().isHttp()) {
                context.relayHttpFirstPackage();
            }
            context.setWriteQueue(tc.get().getWriteQueue());
            Channel visitor = context.getVisitor();
            visitor.config().setOption(ChannelOption.AUTO_READ, true);
        } else {
            context.fireEvent(StreamEvent.STREAM_OPEN_FAILURE);
        }
        context.removeVariable("tunnelId");
    }

    private void handleDirectTunnel(StreamContext context) {
        //多路复用隧道不用额外处理，已经预先处理过了
        if (context.isMux()) {
            return;
        }
        boolean encrypt = context.isEncrypt();
        boolean compress = context.isCompress();

        Channel tunnel = context.getTunnel();
        Channel visitor = context.getVisitor();
        ChannelPipeline tunnelPipeline = tunnel.pipeline();
        ChannelPipeline visitorPipeline = visitor.pipeline();


        String[] handlersToRemove = {
                NettyConstants.TMSP_CODEC,
                NettyConstants.CONTROL_FRAME_HANDLER
        };

        for (String handlerName : handlersToRemove) {
            if (tunnelPipeline.get(handlerName) != null) {
                tunnelPipeline.remove(handlerName);
            }
        }
        if (visitorPipeline.get(NettyConstants.TCP_VISITOR_HANDLER) != null) {
            visitorPipeline.remove(NettyConstants.TCP_VISITOR_HANDLER);
        }
        if (visitorPipeline.get(NettyConstants.HTTP_VISITOR_HANDLER) != null) {
            visitorPipeline.remove(NettyConstants.HTTP_VISITOR_HANDLER);
        }
        if (!encrypt && tunnelPipeline.get(NettyConstants.TLS_HANDLER) != null) {
            TlsHandlerCleanup.removeTlsGracefully(tunnelPipeline);
        } else {
            SslContext tlsContext = TlsContextHolder.get();
            if (tlsContext != null) {
                SslHandler sslHandler = tlsContext.newHandler(tunnel.alloc());
                tunnelPipeline.addFirst(NettyConstants.TLS_HANDLER, sslHandler);
            }
        }
        if (compress) {
            tunnelPipeline.addLast(NettyConstants.SNAPPY_ENCODER, new SnappyEncoder());
            tunnelPipeline.addLast(NettyConstants.SNAPPY_DECODER, new SnappyDecoder());
        } else {
            if (tunnelPipeline.get(NettyConstants.SNAPPY_ENCODER) != null) {
                tunnelPipeline.remove(NettyConstants.SNAPPY_ENCODER);
            }
            if (tunnelPipeline.get(NettyConstants.SNAPPY_DECODER) != null) {
                tunnelPipeline.remove(NettyConstants.SNAPPY_DECODER);
            }
        }

        StreamManager visitorManager = context.getVisitorManager();
        //隧道桥接
        DirectBridgeFactory.bridge(visitorManager, visitor, tunnel, context.getCurrentProtocol());
        logger.debug("独立隧道建立成功: {}", context.getTarget());
    }
}
