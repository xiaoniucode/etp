package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.handler.bridge.AbstractChannelBridge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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

    public ServerChannelBridge(Channel target, String direction,
                               BandwidthLimiter limiter, BridgeRole role,
                               String proxyId, ProtocolType protocol) {
        super(target, direction);
        this.limiter = limiter;
        this.role = role;
        this.proxyId = proxyId;
        this.protocol = protocol;
    }


    @Override
    protected boolean beforeForward(ChannelHandlerContext ctx, Object msg) {
        if (limiter==null){
            return true;
        }
        if (!(msg instanceof ByteBuf buf)) {
            return true;
        }

        if (role == BridgeRole.VISITOR_TO_TUNNEL) {
            // 上传限流
            if (!limiter.tryUpload(buf)) {
                logger.debug("公网->内网 上传流量限速：proxyId-{}",proxyId);
                long waitNanos = limiter.getUploadWaitNanos(buf.readableBytes());
                logger.debug("上传限流，延迟 {} ms 后重试", waitNanos / 1_000_000);

                ReferenceCountUtil.retain(msg);
                ctx.executor().schedule(() -> {
                    if (target.isActive()) {
                        ctx.fireChannelRead(msg);
                    } else {
                        ReferenceCountUtil.release(msg);
                    }
                }, waitNanos, TimeUnit.NANOSECONDS);

                return false;
            }
        }

        if (role == BridgeRole.TUNNEL_TO_VISITOR) {
            // 下载限流
            if (!limiter.tryDownload(buf)) {
                logger.debug("内网 -> 公网 下载流量限速：proxyId-{}",proxyId);
                long waitNanos = limiter.getDownloadWaitNanos(buf.readableBytes());
                logger.debug("下载限流，延迟 {} ms 后重试", waitNanos / 1_000_000);

                // 延迟后重新放回队列
                ReferenceCountUtil.retain(msg);
                ctx.executor().schedule(() -> {
                    //检查目标是否还在
                    if (target.isActive()) {
                        ctx.fireChannelRead(msg);
                    } else {
                        ReferenceCountUtil.release(msg);
                        ctx.close();
                    }
                }, waitNanos, TimeUnit.NANOSECONDS);

                return false;
            }
        }
        return true;
    }
}
