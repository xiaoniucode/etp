package com.xiaoniucode.etp.server.transport;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import io.netty.channel.Channel;

/**
 * 服务端桥接器工厂
 * @author xiaoniucode
 */
public class DirectBridgeFactory {

    /**
     * 创建双向桥接（带限流）
     */
    public static void bridge(StreamManager visitorManager, Channel visitor, Channel tunnel,
                              BandwidthLimiter limiter, ProtocolType protocol) {
        // 上传 受limitOut 限制
        visitor.pipeline().addLast(new DirectChannelBridge(
                visitorManager,
                tunnel,
                "visitor->tunnel",
                limiter,
                BridgeRole.VISITOR_TO_TUNNEL,
                protocol
        ));

        //下载 受limitIn 限制
        tunnel.pipeline().addLast(new DirectChannelBridge(
                visitorManager,
                visitor,
                "tunnel->visitor",
                limiter,
                BridgeRole.TUNNEL_TO_VISITOR,
                protocol
        ));
    }

    /**
     * 创建无限流双向桥接器
     */
    public static void bridge(StreamManager VisitorManager, Channel visitor, Channel tunnel, ProtocolType protocol) {
        bridge(VisitorManager,visitor, tunnel, null, protocol);
    }
}