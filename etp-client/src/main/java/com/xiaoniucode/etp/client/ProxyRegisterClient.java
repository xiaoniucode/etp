package com.xiaoniucode.etp.client;

import com.xiaoniucode.etp.core.protocol.TunnelMessage.ProxyRequest;
import com.xiaoniucode.etp.core.protocol.TunnelMessage.Message;
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
    public void registerProxy(ProxyRequest request) {
        Message message = Message.newBuilder()
            .setType(Message.Type.PROXY_REGISTER)
            .setPayload(request.toByteString())
            .build();
        Channel controlChannel = ChannelManager.getControlChannel();
        controlChannel.writeAndFlush(message);
    }

    /**
     * 注销端口映射
     */
    public void unregisterProxy(Integer proxyId) {
        Channel controlChannel = ChannelManager.getControlChannel();
        Message message = Message.newBuilder()
            .setType(Message.Type.PROXY_UNREGISTER)
            .setExt(proxyId.toString())
            .build();
        controlChannel.writeAndFlush(message);
    }
}
