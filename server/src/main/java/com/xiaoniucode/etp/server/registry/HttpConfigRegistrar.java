package com.xiaoniucode.etp.server.registry;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.RouteConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.statemachine.stream.StreamManager;

import com.xiaoniucode.etp.server.vhost.DomainBinding;
import com.xiaoniucode.etp.server.vhost.DomainManager;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.javers.core.diff.Diff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpConfigRegistrar implements ConfigRegistrar {
    private final InternalLogger logger= InternalLoggerFactory.getInstance(HttpConfigRegistrar.class);
    @Autowired
    private DomainManager domainManager;
    @Autowired
    private StreamManager streamManager;
    @Autowired
    private AgentManager agentManager;

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
        if (config.getProxyId() == null) {
            throw new EtpException("proxyId 不能为空");
        }
        if (!StringUtils.hasText(config.getName())) {
            throw new EtpException("代理名不能为空！");
        }
    }

    @Override
    public RegisterResult register(ProxyConfig config) throws EtpException {
        String proxyId = config.getProxyId();
        config.setProxyId(proxyId);
        RouteConfig routeConfig = config.getRouteConfig();
        List<DomainBinding> domains = domainManager.register(proxyId, routeConfig);
        agentManager.getAgentContext(config.getAgentId()).ifPresent(agentContext -> {
            agentManager.addProxyContextIndex(config.getProxyId(), agentContext);
        });
       return RegisterResult.of(config,domains);
    }

    @Override
    public RegisterResult reregister(ProxyConfig oldConfig, ProxyConfig newConfig, Diff diff) throws EtpException {
        newConfig.setProxyId(oldConfig.getProxyId());
        if (oldConfig.getRouteConfig().getDomainType() != newConfig.getRouteConfig().getDomainType()) {
            domainManager.unregister(oldConfig.getProxyId());
            List<DomainBinding> domains = domainManager.register(oldConfig.getProxyId(), newConfig.getRouteConfig());
            return RegisterResult.of(newConfig,domains);
        }
        List<DomainBinding> domains = domainManager.getBoundDomains(oldConfig.getProxyId());
        return RegisterResult.of(newConfig,domains);
    }

    @Override
    public void unregister(ProxyConfig config) throws EtpException {
        logger.debug("HTTP代理配置删除，清理关联资源");
        List<DomainBinding> boundDomains = domainManager.getBoundDomains(config.getProxyId());
        for (DomainBinding domainBinding : boundDomains) {
            streamManager.fireCloseByDomain(domainBinding.getDomain());
        }
        domainManager.unregister(config.getProxyId());
        //todo
        agentManager.getAgentContext(config.getAgentId()).ifPresent(agentContext -> {
            agentManager.removeProxyContextIndex(config.getProxyId());
        });
    }
}
