package com.xiaoniucode.etp.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuxConfig {
    private static final int DEFAULT_CORE = 2;
    private static final int DEFAULT_MAX = 8;
    private static final int MAX_ALLOWED = 8;

    private boolean enable = true;

    /**
     * 核心常驻主连接数
     * 默认 2
     */
    private Integer core = DEFAULT_CORE;

    /**
     * 最大允许的主连接数
     * 默认 8
     */
    private Integer max = DEFAULT_MAX;

    /**
     * 配置校验
     */
    public void validate() {
        if (core == null) {
            core = DEFAULT_CORE;
        }
        if (max == null) {
            max = DEFAULT_MAX;
        }

        if (core < 1) {
            throw new IllegalArgumentException("core 必须 >= 1");
        }
        if (max < 1) {
            throw new IllegalArgumentException("max 必须 >= 1");
        }
        if (core > max) {
            throw new IllegalArgumentException("core 不能大于 max");
        }
        if (max > MAX_ALLOWED) {
            throw new IllegalArgumentException("最大连接数不能超过 " + MAX_ALLOWED);
        }
    }

    public int getEffectiveCore() {
        validate();
        return core;
    }

    public int getEffectiveMax() {
        validate();
        return max;
    }
}