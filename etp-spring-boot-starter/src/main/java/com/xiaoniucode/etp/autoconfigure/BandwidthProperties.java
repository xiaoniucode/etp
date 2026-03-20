package com.xiaoniucode.etp.autoconfigure;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 带宽限制配置
 */
@Getter
@Setter
public class BandwidthProperties implements Serializable {
    /**
     * 总带宽限制（可选）
     */
    private String limit;

    /**
     * 入口带宽限制（可选）
     */
    private String limitIn;

    /**
     * 出口带宽限制（可选）
     */
    private String limitOut;
}
