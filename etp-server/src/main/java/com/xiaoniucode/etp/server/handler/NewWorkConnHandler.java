package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.EtpConstants;
import com.xiaoniucode.etp.core.AbstractTunnelMessageHandler;
import com.xiaoniucode.etp.core.msg.Message;
import com.xiaoniucode.etp.core.msg.NewWorkConn;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 将从内网代理客户端接收到的数据转发给公网访问者
 *
 * @author liuxin
 */
public class NewWorkConnHandler extends AbstractTunnelMessageHandler {
    @Override
    protected void doHandle(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof NewWorkConn workConn) {
            Channel tunnel = ctx.channel();
            Channel visitor = tunnel.attr(EtpConstants.VISITOR_CHANNEL).get();
            if (visitor != null && visitor.isActive()) {
                tunnel.config().setAutoRead(visitor.isWritable());
                if (workConn.getPayload().refCnt() > 0) {
                    visitor.writeAndFlush(workConn.getPayload().retain());
                }
            }
        }
    }
}
