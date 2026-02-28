package com.xiaoniucode.etp.server.transport;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.old.VisitorStreamManager;
import io.netty.channel.Channel;

/**
 * 服务端桥接器工厂
 * @author xiaoniucode
 */
public class ServerBridgeFactory {

    /**
     * 创建双向桥接（带限流）
     */
    public static void bridge(VisitorStreamManager visitorSessionManager, Channel visitor, Channel tunnel,
                              BandwidthLimiter limiter, String proxyId, ProtocolType protocol) {
        // 上传 受limitOut 限制
        visitor.pipeline().addLast(new ServerChannelBridge(
                 visitorSessionManager,
                tunnel,
                "visitor->tunnel",
                limiter,
                BridgeRole.VISITOR_TO_TUNNEL,
                proxyId,
                protocol
        ));

        //下载 受limitIn 限制
        tunnel.pipeline().addLast(new ServerChannelBridge(
                visitorSessionManager,
                visitor,
                "tunnel->visitor",
                limiter,
                BridgeRole.TUNNEL_TO_VISITOR,
                proxyId,
                protocol
        ));
    }

    /**
     * 创建无限流双向桥接器
     */
    public static void bridge(VisitorStreamManager visitorSessionManager, Channel visitor, Channel tunnel, String proxyId, ProtocolType protocol) {
        bridge(visitorSessionManager,visitor, tunnel, null, proxyId,protocol);
    }
}