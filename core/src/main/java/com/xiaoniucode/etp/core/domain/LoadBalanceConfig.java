package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.LoadBalanceStrategy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadBalanceConfig {
    public static final LoadBalanceStrategy DEFAULT_STRATEGY = LoadBalanceStrategy.ROUND_ROBIN;
    /**
     * 负载均衡策略，默认轮询
     */
    private LoadBalanceStrategy strategy=DEFAULT_STRATEGY;
       /**
     * 是否配置了负载均衡
     */
    public boolean hasStrategy() {
        return strategy != null;
    }
}
