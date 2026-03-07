package com.xiaoniucode.etp.server.statemachine.stream.action;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.Target;
import com.xiaoniucode.etp.core.domain.TransportConfig;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancer;
import com.xiaoniucode.etp.server.loadbalance.LoadBalancerFactory;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamContext;
import com.xiaoniucode.etp.server.statemachine.stream.StreamEvent;
import com.xiaoniucode.etp.server.statemachine.stream.StreamState;
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
    protected void doExecute(StreamState from, StreamState to, StreamEvent event, StreamContext context) {
        Channel visitor = context.getVisitor();
        visitor.config().setOption(ChannelOption.AUTO_READ, false);

        ProtocolType protocol = context.getCurrentProtocol();
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
            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setMux(true);
            config.setTransport(transportConfig);
            config.getTargets().add(target);
        }
        if (remotePort==3307){
            config.setRemotePort(3307);
            config.setProtocol(ProtocolType.TCP);
            config.setStatus(ProxyStatus.OPEN);
            config.setProxyId("1001");
            target.setHost("127.0.0.1");
            target.setPort(3306);
            config.getTargets().add(target);
            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setMux(false);
            config.setTransport(transportConfig);
        }
        if (remotePort==8608){
            config.setRemotePort(8608);
            config.setProtocol(ProtocolType.TCP);
            config.setStatus(ProxyStatus.OPEN);
            config.setProxyId("1001");
            target.setHost("127.0.0.1");
            target.setPort(5201);
            config.getTargets().add(target);
            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setMux(false);
            config.setTransport(transportConfig);
        }
        if (protocol.isHttp()){
            config.setProtocol(ProtocolType.HTTP);
            config.setStatus(ProxyStatus.OPEN);
            config.setProxyId("1001");
            target.setHost("127.0.0.1");
            target.setPort(8023);
            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setMux(false);
            config.setTransport(transportConfig);
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
            context.fireEvent(StreamEvent.TARGET_VALIDATED);
        } else {
            logger.debug("访问目标不存在，没有可用隧道");
            context.fireEvent(StreamEvent.STREAM_CLOSE);
        }
    }

    private int getListenerPort(Channel visitor) {
        InetSocketAddress sa = (InetSocketAddress) visitor.localAddress();
        return sa.getPort();
    }
}

//  visitor.attr(ChannelConstants.PROTOCOL_TYPE).set(ProtocolType.HTTP);
//            visitor.attr(ChannelConstants.VISIT_DOMAIN).set(domain);
//            visitor.attr(ChannelConstants.BASIC_AUTH_HEADER).set(basicAuth);
//
//
//String proxyId = domainManager.getProxyId(domain);
//                    if (proxyId == null) {
//        visitor.close();
//                        logger.debug("隧道不存在");
//                        return;
//                                }
//
//ProxyConfig config = proxyManager.getById(proxyId);
//                    if (!config.isOpen()) {
//        visitor.close();
//                        logger.debug("隧道为关闭状态");
//                        return;
//                                }
//                                if (!domainManager.exists(domain)) {
//        logger.warn("没有该域名的代理服务");
//                        visitor.close();
//                        return;
//                                }