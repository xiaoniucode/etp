package com.xiaoniucode.etp.server.transport;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.utils.NettyHttpUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端桥接器 - 支持带宽限流
 * <p>
 * 限流规则：
 * 1. 从公网来的请求（上传）→ 受 limitOut 限制
 * 2. 从内网来的响应（下载）→ 受 limitIn 限制
 * </p>
 */
public class DirectChannelBridge extends AbstractChannelBridge {
    private static final Logger logger = LoggerFactory.getLogger(DirectChannelBridge.class);
    private final StreamManager visitorManager;
    /**
     * 限流器
     */
    private final BandwidthLimiter limiter;
    /**
     * 当前 Bridge 的角色
     */
    private final BridgeRole role;

    private final ProtocolType protocol;

    public  DirectChannelBridge(StreamManager visitorManager, Channel target, String direction,
                                BandwidthLimiter limiter, BridgeRole role,
                                ProtocolType protocol) {
        super(target, direction);
        this.visitorManager = visitorManager;
        this.limiter = limiter;
        this.role = role;
        this.protocol = protocol;
    }


    @Override
    protected boolean beforeForward(ChannelHandlerContext ctx, Object msg) {
        if (limiter == null) {
            return true;
        }
        if (!(msg instanceof ByteBuf buf)) {
            return true;
        }

        if (role == BridgeRole.VISITOR_TO_TUNNEL) {
            //target==visitor
            // 上传限流
            if (!limiter.tryUpload(buf)) {
                logger.debug("上传限流，直接丢弃");
                if (protocol.isHttp()) {
                    //HTTP请求返回 429告诉浏览器，短连接发送后直接关闭
                    NettyHttpUtils.sendHttpTooManyRequests(ctx.channel())
                            .addListener(f -> ChannelUtils.closeOnFlush(ctx.channel()));
                }
                //丢弃数据包，长连接不关闭连接
               // ReferenceCountUtil.release(msg);
                return false;
            }
        }

        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
            // 下载限流
            if (!limiter.tryDownload(buf)) {
                logger.debug("内网 -> 公网 下载流量限速");
                // 丢弃当前数据包
               // ReferenceCountUtil.release(msg);
                if (target.isActive()) {
                    ctx.close();
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
            visitorManager.getStreamContext(target).ifPresent(streamContext -> {
                streamContext.fireEvent(StreamEvent.STREAM_CLOSE);
            });
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
//            visitorSessionManager.closeStream(target, session -> {
//                Channel control = session.getControl();
//                TMSPFrame frame = new TMSPFrame(session.getStreamId(), TMSP.MSG_CLOSE);
//                control.writeAndFlush(frame);
//            });
//        }
       logger.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
           visitorManager.getStreamContext(target).ifPresent(streamContext -> {
               Channel tunnel = streamContext.getTunnel();
               if (tunnel != null) {
                   tunnel.config().setOption(ChannelOption.AUTO_READ, target.isWritable());
               }
           });
        }
        super.channelWritabilityChanged(ctx);
    }
}
