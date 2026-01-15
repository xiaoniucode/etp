package com.xiaoniucode.etp.client;


import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewProxy;
import io.netty.channel.Channel;

/**
 * 映射注册客户端
 *
 * @author liuxin
 */
public class ProxyClient {
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
    public void unregisterProxy() {
        Channel controlChannel = ChannelManager.getControlChannel();
        if (controlChannel != null) {
            Integer proxyId = controlChannel.attr(EtpConstants.PROXY_ID).get();
            Long sessionId = controlChannel.attr(EtpConstants.SESSION_ID).get();
            CloseProxy closeProxy = new CloseProxy(sessionId, proxyId);
            controlChannel.writeAndFlush(closeProxy);
        }
    }
}
