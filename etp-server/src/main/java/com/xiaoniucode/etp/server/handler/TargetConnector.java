package com.xiaoniucode.etp.server.handler;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.server.handler.utils.MessageUtils;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancerFactory;
import com.xiaoniucode.etp.server.manager.domain.VisitorSession;
import com.xiaoniucode.etp.server.manager.session.VisitorSessionManager;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TargetConnector {
    @Autowired
    private LoadBalancerFactory loadBalancerFactory;
    @Autowired
    private VisitorSessionManager visitorSessionManager;

    public void connectToTarget(VisitorSession session) {
        connectToTarget(session, null);
    }

    /**
     * 连接到目标服务，支持负载均衡
     *
     * @param session  会话
     * @param callback 回调连接状态
     */
    public void connectToTarget(VisitorSession session, ConnectionCallback callback) {
        Channel control = session.getControl();
        ProxyConfig config = session.getProxyConfig();
        Target target;
        final LoadBalancer loadBalancer;
        if (config.isLoadBalanceNeeded()) {
            loadBalancer = loadBalancerFactory.getLoadBalancer(config);
            target = loadBalancer.select(config.getTargets(), config.getProxyId());
        } else {
            loadBalancer = null;
            target = config.getSingleTarget();
        }
        Message.ControlMessage message = MessageUtils
                .buildNewVisitorConn(session.getSessionId(),
                        target.getHost(),
                        target.getPort(),
                        config.getCompress(),
                        config.getEncrypt());
        control.writeAndFlush(message).addListener(f -> {
            if (f.isSuccess()) {
                if (callback != null) {
                    callback.onSuccess(session, target);
                }
                session.setCurrentTarget(target);
                session.setCurrentLoadBalancer(loadBalancer);
            } else {
                if (callback != null) {
                    callback.onFailure(session, target, f.cause());
                }
                visitorSessionManager.disconnect(session.getVisitor(), null);
                //重试策略...
                // scheduleRetry(session, callback);
            }
        });
    }
}
