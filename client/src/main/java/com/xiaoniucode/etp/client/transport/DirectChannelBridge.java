package com.xiaoniucode.etp.client.transport;

import com.xiaoniucode.etp.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象管道桥接器，用于实现两个channel双向转发数据
 */
public class DirectChannelBridge extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(DirectChannelBridge.class);
    /**
     * 对端Channel - 数据要写给的对方
     */
    protected final Channel target;
    /**
     * 桥接方向描述 - 用于日志
     */
    protected final String direction;

    public DirectChannelBridge(Channel target, String direction) {
        this.target = target;
        this.direction = direction;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!target.isActive() || !target.isOpen() || !target.isWritable()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        if (!beforeForward(ctx, msg)) {
            return;
        }
        forwardToTarget(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelUtils.closeOnFlush(target);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("数据转发异常: 数据流方向={}, ", direction, cause);
        ChannelUtils.closeOnFlush(ctx.channel());
        ChannelUtils.closeOnFlush(target);
    }

    /**
     * 转发前的预处理 - 子类可重写
     *
     * @param ctx ChannelHandlerContext
     * @param msg 数据
     * @return true:继续转发, false:取消转发
     */
    protected boolean beforeForward(ChannelHandlerContext ctx, Object msg) {
        return true;
    }

    private void forwardToTarget(ChannelHandlerContext ctx, Object msg) {
        if (!target.isActive()) {
            ReferenceCountUtil.release(msg);
            return;
        }
        ReferenceCountUtil.retain(msg);
        target.writeAndFlush(msg).addListener((ChannelFutureListener) f -> {
            try {
                if (!f.isSuccess()) {
                    logger.error("消息转发失败", f.cause());
                    //关闭当前读数据的通道
                    ChannelUtils.closeOnFlush(ctx.channel());
                    //关闭要写入的目标通道
                    ChannelUtils.closeOnFlush(target);
                } else {
                    logger.debug("转发成功");
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        });
    }

}
