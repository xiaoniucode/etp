package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.server.loadbalance.LeastConnectionLoadBalancer;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 最少连接负载均衡管理器，用于管理连接计数
 */
public class LeastConnManager {
    private static final Logger logger = LoggerFactory.getLogger(LeastConnManager.class);

    /**
     * 增加目标服务器的连接数
     *
     * @param visitorSession 访问者会话
     */
    public static void incrementConnection(VisitorSession visitorSession) {
        if (visitorSession.isUsingLoadBalancer()) {
            LoadBalancer currentLoadBalancer = visitorSession.getCurrentLoadBalancer();
            if (currentLoadBalancer instanceof LeastConnectionLoadBalancer lb) {
                lb.incrementConnection(visitorSession.getCurrentTarget());
                logger.debug("增加连接数成功，会话ID: {}, 目标服务器: {}", 
                    visitorSession.getSessionId(), visitorSession.getCurrentTarget());
            }
        }
    }
    /**
     * 减少目标服务器的连接数
     * @param session 访问者会话
     */
    public static void decrementConnection(VisitorSession session) {
        if (session.isUsingLoadBalancer() && !session.isConnectionCountDecremented()) {
            if (session.getCurrentLoadBalancer() instanceof LeastConnectionLoadBalancer lb) {
                lb.decrementConnection(session.getCurrentTarget());
                logger.debug("减少连接数成功，会话ID: {}, 目标服务器: {}", 
                    session.getSessionId(), session.getCurrentTarget());
            }
        }
    }
}

