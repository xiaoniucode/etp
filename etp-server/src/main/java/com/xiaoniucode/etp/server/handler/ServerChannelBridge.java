package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.handler.bridge.AbstractChannelBridge;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.handler.utils.MessageUtils;
import com.xiaoniucode.etp.server.handler.utils.NettyHttpUtils;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.util.ReferenceCountUtil;
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
public class ServerChannelBridge extends AbstractChannelBridge {
    private static final Logger logger = LoggerFactory.getLogger(ServerChannelBridge.class);
    private final VisitorSessionManager visitorSessionManager;
    /**
     * 限流器
     */
    private final BandwidthLimiter limiter;
    /**
     * 当前 Bridge 的角色
     */
    private final BridgeRole role;
    /**
     * 代理 ID
     */
    private final String proxyId;

    private final ProtocolType protocol;

    public ServerChannelBridge(VisitorSessionManager visitorSessionManager, Channel target, String direction,
                               BandwidthLimiter limiter, BridgeRole role,
                               String proxyId, ProtocolType protocol) {
        super(target, direction);
        this.visitorSessionManager = visitorSessionManager;
        this.limiter = limiter;
        this.role = role;
        this.proxyId = proxyId;
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
                logger.debug("上传限流，直接丢弃：proxyId-{}", proxyId);
                if (protocol.isHttp()) {
                    //HTTP请求返回 429告诉浏览器，短连接发送后直接关闭
                    NettyHttpUtils.sendHttpTooManyRequests(ctx.channel())
                            .addListener(f -> ChannelUtils.closeOnFlush(ctx.channel()));
                }
                //丢弃数据包，长连接不关闭连接
                ReferenceCountUtil.release(msg);
                return false;
            }
        }

        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
            // 下载限流
            if (!limiter.tryDownload(buf)) {
                logger.debug("内网 -> 公网 下载流量限速：proxyId-{}", proxyId);
                // 丢弃当前数据包
                ReferenceCountUtil.release(msg);
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
            logger.debug("访问者连接断开，释放资源");
            visitorSessionManager.disconnect(target, session -> {
                Channel control = session.getControl();
                Message.ControlMessage message = MessageUtils
                        .buildCloseProxy(session.getSessionId());
                control.writeAndFlush(message);
            });
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
            visitorSessionManager.disconnect(target, session -> {
                Channel control = session.getControl();
                Message.ControlMessage message = MessageUtils.buildCloseProxy(session.getSessionId());
                control.writeAndFlush(message);
            });
        }
        logger.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
            VisitorSession visitorSession = visitorSessionManager.getVisitorSession(target);
            Channel tunnel = visitorSession.getTunnel();
            if (tunnel != null) {
                tunnel.config().setOption(ChannelOption.AUTO_READ, target.isWritable());
            }
        }
        super.channelWritabilityChanged(ctx);
    }
}
