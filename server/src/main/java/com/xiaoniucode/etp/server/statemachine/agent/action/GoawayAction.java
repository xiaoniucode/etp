package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.utils.ChannelUtils;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;
import com.xiaoniucode.etp.server.transport.connection.DirectConnectionPool;
import com.xiaoniucode.etp.server.transport.connection.MultiplexConnectionPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class GoawayAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(GoawayAction.class);
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private DirectConnectionPool directConnectionPool;
    @Autowired
    private MultiplexConnectionPool multiplexConnectionPool;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        AgentInfo agentInfo = context.getAgentInfo();
        if (agentInfo == null) {
            logger.warn("客户端断开，未找到客户端信息，连接ID：{}", context.getConnectionId());
            return;
        }
        String agentId = agentInfo.getAgentId();
        logger.debug("{} 客户端断开，开始清理资源", agentId);
        try {
            // 清理流资源
            streamManager.fireCloseByAgent(context.getAgentId());
            // cleanupStreams(agentId);
            logger.debug("清理客户端 {} 所有连接", agentId);
            // 清理隧道资源
            directConnectionPool.offline(agentId);
            multiplexConnectionPool.offline(agentId);
            agentManager.removeAgentContext(agentId);
            // 清理代理资源
            //cleanupAgent(agentId);

            // 清理关联的 Channel
            //cleanupChannels(context);

            // 清理Context 中的临时数据
            //cleanupContextData(context);
            if (event==AgentEvent.LOCAL_GOAWAY){
                Channel control = context.getControl();
                control.writeAndFlush(new TMSPFrame(0, TMSP.MSG_GOAWAY)).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        logger.debug("{} GOAWAY 发送失败（可能连接已断）", agentId);
                    }
                    ChannelUtils.closeOnFlush(control);
                });
            }

            logger.info("{} 客户端资源清理完成", agentId);
        } catch (Exception e) {
            logger.error("{} 资源清理过程中发生异常", agentId, e);
        }

    }
}
