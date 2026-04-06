package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.core.enums.LoadBalanceType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class LoadBalanceConfig {
    public static final LoadBalanceType DEFAULT_STRATEGY = LoadBalanceType.ROUND_ROBIN;
    /**
     * 负载均衡策略，默认轮询
     */
    private LoadBalanceType strategy=DEFAULT_STRATEGY;
       /**
     * 是否配置了负载均衡
     */
    public boolean hasStrategy() {
        return strategy != null;
    }
}
