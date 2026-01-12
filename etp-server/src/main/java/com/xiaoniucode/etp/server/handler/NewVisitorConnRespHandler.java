package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewVisitorConnResp;
import com.xiaoniucode.etp.server.handler.visitor.HttpVisitorHandler;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理来自代理客户端连接成功消息
 *
 * @author liuxin
 */
public class NewVisitorConnRespHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(NewVisitorConnRespHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof NewVisitorConnResp resp) {
            long sessionId = resp.getSessionId();
            String secretKey = resp.getSecretKey();
            Channel controlChannel = ChannelManager.getControlChannelBySecretKey(secretKey);
            if (controlChannel == null) {
                return;
            }
            Channel visitorChannel = ChannelManager.getClientChannel(controlChannel, sessionId);
            Channel dataChannel = ctx.channel();
            if (dataChannel == null) {
                return;
            }
            dataChannel.attr(EtpConstants.SECRET_KEY).set(secretKey);
            dataChannel.attr(EtpConstants.SESSION_ID).set(sessionId);
            visitorChannel.attr(EtpConstants.CONNECTED).set(true);
            //访问者channel与数据隧道channel双向绑定
            dataChannel.attr(EtpConstants.VISITOR_CHANNEL).set(visitorChannel);
            visitorChannel.attr(EtpConstants.DATA_CHANNEL).set(dataChannel);
            visitorChannel.config().setOption(ChannelOption.AUTO_READ, true);
            logger.debug("已连接到目标服务");
            //通知http处理器已经连接成功
            HttpVisitorHandler.connectToTarget(ctx, visitorChannel);
        }
    }
}
