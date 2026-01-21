package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.msg.Login;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.server.manager.ClientManager;
import com.xiaoniucode.etp.server.manager.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理客户端连接认证
 *
 * @author liuxin
 */
public class LoginHandler extends AbstractTunnelMessageHandler {
    private final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof Login login) {
            String secretKey = login.getSecretKey();
            if (!ClientManager.hasClient(secretKey)) {
                logger.error("客户端: {} 认证失败", secretKey);
                ctx.channel().close();
                return;
            }
            Channel control = ChannelManager.getControl(secretKey);
            if (control != null) {
                //已经注册
                ctx.channel().close();
            }

            ChannelManager.registerClient(ctx.channel(), login);
            logger.debug("客户端: {} 注册成功", secretKey);
        }
    }
}
