package com.xiaoniucode.etp.server.loadbalance;


import com.xiaoniucode.etp.core.domain.Target;

import java.util.List;

/**
 * 负载均衡器接口
 */
public interface LoadBalancer {

    /**
     * 选择一个目标
     *
     * @param targets 可用的目标列表
     * @param proxyId 代理 ID
     * @return 选中的目标，如果没有可用返回null
     */
    Target select(List<Target> targets, String proxyId);
}