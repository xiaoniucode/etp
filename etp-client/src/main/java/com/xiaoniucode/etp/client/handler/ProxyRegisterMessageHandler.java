package com.xiaoniucode.etp.client.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.MessageHandler;
import com.xiaoniucode.etp.core.protocol.TunnelMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理端口映射注册结果信息
 *
 * @author liuxin
 */
public class ProxyRegisterMessageHandler implements MessageHandler {
    private final Logger logger = LoggerFactory.getLogger(ProxyRegisterMessageHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, TunnelMessage.Message msg) throws InvalidProtocolBufferException {
        Channel controlChannel = ctx.channel();
        TunnelMessage.ProxyResponse response = TunnelMessage.ProxyResponse.parseFrom(msg.getPayload());
        //用于下线时通知服务端清理对应的资源
        controlChannel.attr(EtpConstants.PROXY_ID).set(response.getProxyId());
        String serverAddr = controlChannel.attr(EtpConstants.SERVER_DDR).get();
        logger.info("公网访问地址：{}:{}", serverAddr, response.getExternalUrl());
    }
}
