package com.xiaoniucode.etp.client;


import com.xiaoniucode.etp.client.helper.ProxyRespHelper;
import com.xiaoniucode.etp.client.manager.ChannelManager;
import com.xiaoniucode.etp.core.msg.CloseProxy;
import com.xiaoniucode.etp.core.msg.NewProxy;
import com.xiaoniucode.etp.core.msg.NewProxyResp;
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
            NewProxyResp resp = ProxyRespHelper.get();
            CloseProxy closeProxy = new CloseProxy(resp.getSessionId(), resp.getProxyId());
            controlChannel.writeAndFlush(closeProxy);
        }
    }
}
