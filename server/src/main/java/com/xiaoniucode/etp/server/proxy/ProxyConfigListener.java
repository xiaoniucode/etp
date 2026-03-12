package com.xiaoniucode.etp.server.proxy;

import com.xiaoniucode.etp.core.domain.ProxyConfig;

public interface ProxyConfigListener {
    /**
     * 代理添加回调
     */
    void onAdded(ProxyConfig config);

    /**
     * 代理更新回调
     */
    void onUpdated(ProxyConfig oldConfig, ProxyConfig newConfig);

    /**
     * 代理删除回调
     */
    void onDeleted(ProxyConfig config);

    /**
     * 状态变更回调
     */
    void onStatusChanged(ProxyConfig config, boolean newStatus);
}