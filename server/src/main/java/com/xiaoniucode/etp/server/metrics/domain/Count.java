package com.xiaoniucode.etp.server.metrics.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 总流量统计实体类
 *
 * @author liuxin
 */
@Getter
@Setter
@ToString
public class Count {
    /**
     * 入站字节数
     */
    private long in;
    /**
     * 出站字节数
     */
    private long out;
}
