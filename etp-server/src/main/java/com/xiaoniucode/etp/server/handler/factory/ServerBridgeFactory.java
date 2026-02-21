package com.xiaoniucode.etp.server.handler.factory;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.handler.BandwidthLimiter;
import com.xiaoniucode.etp.server.handler.BridgeRole;
import com.xiaoniucode.etp.server.handler.ServerChannelBridge;
import io.netty.channel.Channel;

/**
 * 服务端桥接器工厂
 * @author xiaoniucode
 */
public class ServerBridgeFactory {

    /**
     * 创建双向桥接（带限流）
     */
    public static void bridge(Channel visitor, Channel tunnel,
                              BandwidthLimiter limiter, String proxyId, ProtocolType protocol) {
        // 上传 受limitOut 限制
        visitor.pipeline().addLast(new ServerChannelBridge(
                tunnel,
                "visitor->tunnel",
                limiter,
                BridgeRole.VISITOR_TO_TUNNEL,
                proxyId,
                protocol
        ));

        //下载 受limitIn 限制
        tunnel.pipeline().addLast(new ServerChannelBridge(
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
    public static void bridge(Channel visitor, Channel tunnel, String proxyId,ProtocolType protocol) {
        bridge(visitor, tunnel, null, proxyId,protocol);
    }
}