package io.github.lxien.orbien.client.statemachine.agent.action;

import io.github.lxien.orbien.client.statemachine.agent.AgentContext;
import io.github.lxien.orbien.client.statemachine.agent.AgentEvent;
import io.github.lxien.orbien.client.statemachine.agent.AgentState;
import io.github.lxien.orbien.client.statemachine.stream.StreamEvent;
import io.github.lxien.orbien.client.statemachine.stream.StreamManager;
import io.github.lxien.orbien.core.message.TMSP;
import io.github.lxien.orbien.core.message.TMSPFrame;
import io.github.lxien.orbien.core.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


public class GoawayAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(GoawayAction.class);

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext agentContext) {
        agentContext.markShuttingDown();
        logger.info("正在关闭客户端");
        StreamManager.getStreamContexts().forEach(streamContext -> {
            streamContext.fireEvent(StreamEvent.STREAM_LOCAL_CLOSE);
        });
        Channel control = agentContext.getControl();
        if (event == AgentEvent.LOCAL_GOAWAY && from == AgentState.CONNECTED && control != null && control.isActive()) {
            control.writeAndFlush(new TMSPFrame(agentContext.getConnectionId(), TMSP.MSG_GOAWAY))
                    .addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            logger.debug("通知服务端断开连接失败（可能连接已断）");
                        }
                    });
        }
        agentContext.getPoolManager().closeAll();
        if (control != null) {
            ChannelUtils.closeOnFlush(control);
        }
        agentContext.getTunnelClient().stop();
        logger.info("Orbien 客户端已停止");
    }
}