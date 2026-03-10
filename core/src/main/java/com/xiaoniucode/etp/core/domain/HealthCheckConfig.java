package com.xiaoniucode.etp.core.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HealthCheckConfig {
    /**
     * 是否开启健康检查
     */
    private boolean enable;
    /**
     * 检查间隔
     */
    private Integer interval;
    /**
     * 连接超时时间
     */
    private Integer timeout;
    /**
     * 最大失败次数，超过次数将instance剔除
     */
    private Integer maxFail;
}
