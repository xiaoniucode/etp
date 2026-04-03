package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.RouteConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.generator.UUIDGenerator;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;

import com.xiaoniucode.etp.server.vhost.DomainBinding;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import org.javers.core.diff.Diff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpConfigRegistrar implements ConfigRegistrar {
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private UUIDGenerator uuidGenerator;

    @Override
    public boolean supports(ProxyConfig config) {
        return config.isHttp();
    }

    @Override
    public void validate(ProxyConfig config) throws EtpException {
        RouteConfig routeConfig = config.getRouteConfig();
        if (routeConfig == null) {
            throw new EtpException("HTTP(s)协议必须配置域名");
        }
        if (!StringUtils.hasText(config.getName())) {
            throw new EtpException("代理名不能为空！");
        }
    }

    @Override
    public void register(ProxyConfig config) throws EtpException {
        String proxyId = uuidGenerator.uuid32();
        config.setProxyId(proxyId);
        RouteConfig routeConfig = config.getRouteConfig();
        domainManager.register(proxyId, routeConfig);
    }

    @Override
    public void reregister(ProxyConfig oldConfig, ProxyConfig newConfig, Diff diff) throws EtpException {
        newConfig.setProxyId(oldConfig.getProxyId());
        //todo 如果域名配置发生变化 重建域名
        domainManager.unregister(oldConfig.getProxyId());
        domainManager.register(oldConfig.getProxyId(), newConfig.getRouteConfig());
    }

    @Override
    public void unregister(ProxyConfig config) throws EtpException {
        domainManager.unregister(config.getProxyId());
        List<DomainBinding> boundDomains = domainManager.getBoundDomains(config.getProxyId());
        for (DomainBinding domainBinding : boundDomains) {
            streamManager.closeStreams(domainBinding.getDomain());
        }
        agentManager.getAgentContext(config.getAgentId()).ifPresent(agentContext -> {
            agentManager.removeProxyContextIndex(config.getProxyId());
        });
    }
}
