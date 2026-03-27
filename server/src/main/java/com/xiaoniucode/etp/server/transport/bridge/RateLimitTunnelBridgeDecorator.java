//package com.xiaoniucode.etp.server.transport.bridge;
//
//import com.xiaoniucode.etp.core.enums.ProtocolType;
//import com.xiaoniucode.etp.core.transport.AbstractTunnelBridgeDecorator;
//import com.xiaoniucode.etp.core.transport.TunnelBridge;
//import com.xiaoniucode.etp.core.transport.TunnelEntry;
//import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
//import com.xiaoniucode.etp.server.transport.BandwidthLimiter;
//import com.xiaoniucode.etp.server.utils.NettyHttpUtils;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelOption;
//import io.netty.util.internal.logging.InternalLogger;
//import io.netty.util.internal.logging.InternalLoggerFactory;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//public class RateLimitTunnelBridgeDecorator extends AbstractTunnelBridgeDecorator {
//    private static final InternalLogger logger = InternalLoggerFactory.getInstance(RateLimitTunnelBridgeDecorator.class);
//    private final StreamContext streamContext;
//    private final TunnelEntry tunnelEntry;
//    private final Channel visitor;
//
//    public RateLimitTunnelBridgeDecorator(TunnelBridge delegate, StreamContext streamContext) {
//        super(delegate);
//        this.streamContext = streamContext;
//        this.tunnelEntry = streamContext.getTunnelEntry();
//        this.visitor = streamContext.getVisitor();
//    }
//
//    @Override
//    public void forwardToLocal(ByteBuf payload) {
//        BandwidthLimiter limiter = streamContext.getBandwidthLimiter();
//        if (limiter == null || limiter.tryUpload(payload)) {
//            logger.debug("访问速度正常，继续转发：streamId={}", streamContext.getStreamId());
//            return;
//        }
//        int bytes = payload.readableBytes();
//        long waitNanos = limiter.getUploadWaitNanos(bytes);
//        logger.warn("访问速度太快，触发限流：streamId={} bytes={} 等待 {} ms", streamContext.getStreamId(), bytes, waitNanos / 1_000_000);
//        //响应HTTP 上传时发 429 + close
//        ProtocolType protocol = streamContext.getCurrentProtocol();
//        if (protocol != null && protocol.isHttp()) {
//            NettyHttpUtils.sendHttpTooManyRequests(visitor)
//                    .addListener(f -> {
//                        // 等待 waitNanos 后恢复读取
//                        scheduleResume(visitor, waitNanos);
//                    });
//        } else {
//            // 等待 waitNanos 后恢复读取
//            visitor.config().setOption(ChannelOption.AUTO_READ, false);
//            scheduleResume(visitor, waitNanos);
//        }
//        logger.debug("发送限流时从访问流收到的数据包到内网");
//        delegate.forwardToLocal(payload);
//    }
//
////    @Override
////    public void forwardToRemote(ByteBuf in) {
////        BandwidthLimiter limiter = streamContext.getBandwidthLimiter();
////        if (limiter == null || limiter.tryDownload(in)) {
////            logger.debug("访问速度正常，继续转发：streamId={}", streamContext.getStreamId());
////            delegate.forwardToRemote(in);
////            return;
////        }
////        Channel tunnel = tunnelEntry.getChannel();
////        int bytes = in.readableBytes();
////        long waitNanos = limiter.getDownloadWaitNanos(bytes);
////        logger.warn("访问速度太快，触发限流，暂停从隧道读取：streamId={} bytes={} 等待 {} ms", streamContext.getStreamId(), bytes, waitNanos / 1_000_000);
////        tunnel.config().setOption(ChannelOption.AUTO_READ, false);
////        scheduleResume(tunnel, waitNanos);
////        logger.debug("发送限流时从内网收到的数据包给访问者");
////        delegate.forwardToRemote(in);
////
////    }
//
//    private void scheduleResume(Channel channel, long waitNanos) {
//        if (channel == null) return;
//        long waitMillis = Math.max(1, waitNanos / 1_000_000);
//        channel.eventLoop().schedule(() -> {
//            channel.config().setOption(ChannelOption.AUTO_READ, true);
//            logger.debug("限流恢复，继续读取：streamId={}", streamContext.getStreamId());
//            channel.read();
//        }, waitMillis, TimeUnit.MILLISECONDS);
//    }
//}