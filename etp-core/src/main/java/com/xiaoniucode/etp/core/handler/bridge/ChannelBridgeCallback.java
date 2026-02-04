package com.xiaoniucode.etp.core.handler.bridge;

import io.netty.channel.Channel;

public interface ChannelBridgeCallback {
    /**
     * 通道关闭时回调
     *
     * @param channel 当前关闭的通道
     * @param peer    对端通道
     */
    default void onChannelInactive(Channel channel, Channel peer) {
    }

    /**
     * 通道异常时回调
     *
     * @param channel 发生异常的通道
     * @param peer    对端通道
     * @param cause   异常原因
     */
    default void onExceptionCaught(Channel channel, Channel peer, Throwable cause) {
    }

    /**
     * 通道可写性变化时回调
     *
     * @param channel    可写性变化的通道
     * @param peer       对端通道
     * @param isWritable 当前通道是否可写
     */
    default void onChannelWritabilityChanged(Channel channel, Channel peer, boolean isWritable) {
    }
}