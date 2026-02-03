package com.xiaoniucode.etp.server.task;

import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.server.handler.utils.MessageWrapper;
import com.xiaoniucode.etp.server.manager.domain.AgentSession;
import com.xiaoniucode.etp.server.manager.session.AgentSessionManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 定时任务，用于自动清理无效会话信息
 */
@Component
public class SessionCleanupTask {
    private final Logger logger = LoggerFactory.getLogger(SessionCleanupTask.class);
    /**
     * 连接超时时间 5分钟
     */
    private static final long HEARTBEAT_TIMEOUT = 300000L;
    /**
     * 定时任务运行间隔时间1分钟
     */
    private static final long CLEANUP_INTERVAL = 60000L;
    @Autowired
    private AgentSessionManager agentSessionManager;

    /**
     * 自动清理客户端心跳超时的会话
     */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL)
    public void cleanupExpiredAgentSessions() {
        logger.debug("自动清理心跳超时客户端会话信息定时任务");
        Collection<AgentSession> agentSessions = agentSessionManager.getAllAgentSessions();
        if (agentSessions != null) {
            long now = System.currentTimeMillis();
            agentSessions.stream().filter(conn -> (now - conn.getLastHeartbeat()) > HEARTBEAT_TIMEOUT).forEach(conn -> {
                if (logger.isWarnEnabled()) {
                    logger.warn("清理心跳超时客户端会话: {}", conn.getClientId());
                }
                //尝试向客户端发送心跳超时消息
                Channel control = conn.getControl();
                Message.ControlMessage message = MessageWrapper.heartbeatTimeout();
                control.writeAndFlush(message);
                agentSessionManager.disconnect(conn.getControl());
            });
        }
    }
}
