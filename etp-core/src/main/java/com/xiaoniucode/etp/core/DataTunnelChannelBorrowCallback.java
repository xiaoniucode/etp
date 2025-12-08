package com.xiaoniucode.etp.core;

import io.netty.channel.Channel;

/**
 * @author liuxin
 */
public interface DataTunnelChannelBorrowCallback {
    /**
     * 成功获取到数据隧道通道
     *
     * @param channel 通道
     */
    void success(Channel channel);

    /**
     * 失败获取
     *
     * @param cause 失败原因
     */
    void fail(Throwable cause);

}
