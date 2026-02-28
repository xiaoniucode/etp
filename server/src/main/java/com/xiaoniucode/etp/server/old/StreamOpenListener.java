//package com.xiaoniucode.etp.server.statemachine.listener.visitor;
//
//import com.xiaoniucode.etp.core.codec.compress.SnappyDecoder;
//import com.xiaoniucode.etp.core.codec.compress.SnappyEncoder;
//import com.xiaoniucode.etp.core.domain.BandwidthConfig;
//import com.xiaoniucode.etp.core.domain.ProxyConfig;
//import com.xiaoniucode.etp.core.message.TMSPFrame;
//import com.xiaoniucode.etp.core.statemachine.*;
//import com.xiaoniucode.etp.server.handler.BandwidthLimiter;
//import com.xiaoniucode.etp.server.handler.ServerBridgeFactory;
//import com.xiaoniucode.etp.server.handler.VisitorStreamContext;
//import com.xiaoniucode.etp.server.manager.LeastConnUtils;
//import com.xiaoniucode.etp.server.manager.ProtocolDetection;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelPipeline;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StreamOpenResponseProcessor implements StreamStateListener {
//    @Override
//    public void onStreamStateChanged(StreamContext ctx, StreamState oldState, StreamState newState, StateEvent trigger, Object payload) {
//
//        ProxyConfig config = visitorStream.getProxyConfig();
//        Channel visitor = visitorStream.getVisitor();
//
//        if (config.hasBandwidthLimit()) {
//            BandwidthConfig bandwidth = config.getBandwidth();
//            BandwidthLimiter bandwidthLimiter = new BandwidthLimiter(bandwidth);
//            visitorStream.setBandwidthLimiter(bandwidthLimiter);
//        }
//        boolean isMuxTunnel = config.isMuxTunnel();
//        if (isMuxTunnel) {
//            // 共享隧道处理 - 多路复用
//            handleSharedTunnel(visitorStream);
//        } else {
//            // 独立隧道 - 独享连接
//            handleDirectTunnel(visitorStream);
//        }
//        // 公共处理
//        completeTunnelSetup(visitorStream);
//    }
//
//    private void handleSharedTunnel(VisitorStreamContext visitorStream) {
//
//    }
//
//
//    private void handleDirectTunnel(VisitorStreamContext visitorStreamContext) {
//        Channel tunnel = visitorStreamContext.getTunnel();
//        ChannelPipeline pipeline = tunnel.pipeline();
//        ProxyConfig config = visitorStreamContext.getProxyConfig();
//        pipeline.remove("controlTunnelHandler");
//        pipeline.remove("idleCheckHandler");
//
//        if (config.isCompressEnabled()) {
//            if (pipeline.get("snappyDecoder") == null) {
//                pipeline.addLast("snappyDecoder", new SnappyDecoder());
//            }
//            if (pipeline.get("snappyEncoder") == null) {
//                pipeline.addLast("snappyEncoder", new SnappyEncoder());
//            }
//        }
//
//        if (!config.isEncryptEnabled()) {
//            //removeTlsGracefully(pipeline);
//        }
//
//        if (config.hasBandwidthLimit()) {
//            ServerBridgeFactory.bridge(visitorSessionManager, visitor, tunnel, visitorStreamContext.getBandwidthLimiter(), config.getProxyId(), config.getProtocol());
//        } else {
//            ServerBridgeFactory.bridge(visitorSessionManager, visitor, tunnel, config.getProxyId(), config.getProtocol());
//        }
//    }
//
//    private void completeTunnelSetup(VisitorStreamContext visitorStream) {
//        LeastConnUtils.incrementConnection(visitorStream);
//        if (ProtocolDetection.isHttp(visitor)) {
//            visitor.attr(ChannelConstants.CONNECTED).set(true);
//            httpVisitorHandler.sendFirstPackage(visitorStream);
//        }
//        visitor.config().setOption(ChannelOption.AUTO_READ, true);
//        logger.debug("已连接到目标服务: proxyName-{}", config.getName());
//    }
//
//    private void handleStreamClose(ChannelHandlerContext ctx, TMSPFrame frame) {
//        int streamId = frame.getStreamId();
//
//        VisitorConnectionContext connCtx = new VisitorConnectionContext();
//        StreamContext stream = connCtx.createStream(streamId);
//        StreamFsm.transition(stream, StateEvent.RECV_STREAM_CLOSE); //关闭流
//
//    }
//}
