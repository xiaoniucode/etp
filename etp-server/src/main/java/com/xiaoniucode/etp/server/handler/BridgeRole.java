package com.xiaoniucode.etp.server.handler;

/**
 * 桥接角色
 */
public enum BridgeRole {
    /**
     * 公网 → 内网（上传）
     */
    VISITOR_TO_TUNNEL,
    /**
     * 内网 → 公网（下载）
     */
    TUNNEL_TO_VISITOR
}