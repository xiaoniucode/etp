package com.xiaoniucode.etp.server.metrics.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 指标数据实体类
 *
 * @author liuxin
 */
@Getter
@Setter
@ToString
public class Metrics {
    /**
     * 唯一标识
     */
    private String key;
    /**
     * 当前活跃连接数量
     */
    private int channels;
    /**
     * 接收总字节数
     */
    private long readBytes;
    /**
     * 发送总字节数
     */
    private long writeBytes;
    /**
     * 接收消息数
     */
    private long readMsgs;
    /**
     * 发送消息数
     */
    private long writeMsgs;
    /**
     * 时间
     */
    private LocalDateTime time;
}
