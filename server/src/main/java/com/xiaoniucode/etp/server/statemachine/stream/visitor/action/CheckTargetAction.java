package com.xiaoniucode.etp.server.statemachine.stream.visitor.action;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancerFactory;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.visitor.ClientStreamState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class CheckTargetAction extends StreamBaseAction {
    private final Logger logger = LoggerFactory.getLogger(CheckTargetAction.class);
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private LoadBalancerFactory loadBalancerFactory;

    @Autowired
    private AgentManager agentManager;
    @Override
    protected void doExecute(ClientStreamState from, ClientStreamState to, ClientStreamEvent event, StreamContext context) {
        Channel visitor = context.getVisitor();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);
        int remotePort = getListenerPort(visitor);
        ProxyConfig config = new ProxyConfig();
        Target target = new Target();
        if (remotePort==8033){
            config.setRemotePort(8033);
            config.setProtocol(ProtocolType.TCP);
            config.setStatus(ProxyStatus.OPEN);
            config.setProxyId("1001");
            target.setHost("127.0.0.1");
            target.setPort(3306);
            config.getTargets().add(target);
        }
        if (remotePort==3307){
            config.setRemotePort(6379);
            config.setProtocol(ProtocolType.TCP);
            config.setStatus(ProxyStatus.OPEN);
            config.setProxyId("1001");
            target.setHost("127.0.0.1");
            target.setPort(6379);
            config.getTargets().add(target);
        }


       agentManager.getAgentContextByProxyId(config.getProxyId()).ifPresent(agent -> {
           context.setControl(agent.getControl());
       });
        //ProxyConfig config = proxyManager.getByRemotePort(remotePort);
        if (config != null) {
            logger.debug("访问目标合法");
            LoadBalancer loadBalancer = loadBalancerFactory.getLoadBalancer(config);
            context.setLoadBalancer(loadBalancer);
            context.setProxyConfig(config);
            context.fireEvent(ClientStreamEvent.TARGET_VALIDATED);
        } else {
            logger.debug("访问目标不存在，没有可用隧道");
            context.fireEvent(ClientStreamEvent.STREAM_CLOSE);
        }
    }

    private int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }
}
