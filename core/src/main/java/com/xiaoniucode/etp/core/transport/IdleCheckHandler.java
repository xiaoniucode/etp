/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.core.transport;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 心跳检查
 *
 * @author xiaoniucode
 */
public class IdleCheckHandler extends IdleStateHandler {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(IdleCheckHandler.class);

    public IdleCheckHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        Channel channel = ctx.channel();
        switch (evt.state()) {
            case WRITER_IDLE:
                channel.writeAndFlush(new TMSPFrame(0, TMSP.MSG_PING));
                logger.debug("发送心跳包给 {}", channel.remoteAddress());
                break;

            case READER_IDLE:
                logger.debug("读空闲超时，关闭连接 {}", channel.remoteAddress());
                channel.close();
                break;
        }
        ctx.fireUserEventTriggered(evt);
    }
}
