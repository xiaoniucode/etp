package com.xiaoniucode.etp.client;


import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewProxy;
import io.netty.channel.Channel;

/**
 * 映射注册客户端
 *
 * @author liuxin
 */
public class ProxyRegisterClient {
    /**
     * 注册端口映射
     */
    public void registerProxy(NewProxy newProxy) {
        Channel controlChannel = ChannelManager.getControlChannel();
        controlChannel.writeAndFlush(newProxy);
    }

    /**
     * 注销端口映射
     */
    public void unregisterProxy(CloseProxy closeProxy) {
        Channel controlChannel = ChannelManager.getControlChannel();
        controlChannel.writeAndFlush(closeProxy);
    }
}
